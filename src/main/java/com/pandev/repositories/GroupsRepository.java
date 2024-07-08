package com.pandev.repositories;

import com.pandev.dto.DTOgroups;
import com.pandev.entities.Groups;
import com.pandev.dto.GroupsDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Репозиторий бизнес модели Groups
 */
public interface GroupsRepository extends JpaRepository<Groups, Integer> {

    /**
     * Верификация наличия записей в таблице.
     * Используется перед начальной загрузкой данных (class LoadData)
     * @return
     */
    @Query(value = "select exists(select * from groups)",nativeQuery = true)
    boolean isExistsData();

    /**
     * Используется для команды telegramBot /viewTree
     * @return
     */
    @Query(value = "select new com.pandev.dto.GroupsDetails(g.levelnum, g.txtgroup) from Groups g order by g.rootnode, g.ordernum")
    List<GroupsDetails> getTreeData();

    Groups findByTxtgroup(String txtgroup );

    List<Groups> findByTxtgroupIn(List<String> ls);

    /**
     * Используется для модульных тестов
     * @return
     */
    @Query(value = "select * from groups where id = (select coalesce(min(id), -1) from groups)", nativeQuery = true)
    Groups firstElement();

    /**
     * Перед добавлением элемента делается выборка записей, которые будут расположены после этой.
     * Критерий расположения записей до или после текущей оценивается в контексте корневого узла
     * и индекса очередности: rootnode and ordernum
     * @param parentid
     * @param rootId
     * @return
     */
    @Query(value = "select id, rootnode, parentnode, txtgroup, ordernum, levelnum from groups " +
            "where ordernum > (select coalesce(max(ordernum), 2147483640) " +
            "from groups where parentnode = :parentid ) " +
            "and rootnode = :rootId order by ordernum desc", nativeQuery = true)
    List<List<Object>> findAllGroupsByParentId(Integer parentid, Integer rootId);

    /**
     * Выборка ВСЕХ записей, связанных через deletednode and ordernum.
     * Используется при удаления элемента
     * @param deletednode удаляемый узел
     * @return
     */
    @Query(value = "select * from groups " +
            "where rootnode = (select rootnode from groups where id = :deletednode ) " +
            "and ordernum in ( select ordernum from groups where parentnode = :deletednode )", nativeQuery = true)
    List<Groups> findAllGroupsForDelete(Integer deletednode);

    /**
     * Используется после удаления узла для обновления поля Groups.ordernum
     * Запускается после обработки скрипта findAllGroupsByParentIdExt.
     * Использование order by g.ordernum обязательно. Т.к. возникают исключения из-за ограничений на уровне таблицы
     * @param rootnode удаляемый узел
     * @return
     */
    @Query(value = "FROM Groups g where g.rootnode = :rootnode and g.ordernum > 0 order by g.ordernum")
    List<Groups> findAllGroupsForUpdateOrdernum(Integer rootnode);

    /**
     * Максимальное значение ordernum из дочерних записей.
     * Если нет данных тогда значение ordernum самого родительского объекта
     * @param rootnode корневой узел
     * @param parentnode родительский узел
     * @return
     */
    @Query(value = "select case " +
            "when exists(Select * from groups where rootnode = :rootnode ) " +
            "then (select max(ordernum) from groups where rootnode = :rootnode ) " +
            "else (select ordernum from groups where id = :parentnode) " +
            "end", nativeQuery = true)
    Integer maxOrdernum(Integer rootnode, Integer parentnode);

    @Query("select g from Groups g where g.rootnode = :rootnode")
    List<Groups> findAllElementByRootNode(Integer rootnode);

    @Query(value = "select exists(select * from groups where txtgroup = lower(trim(:txtgroup)) and parentnode = :parentnode)", nativeQuery = true)
    boolean isExistsBytxtgroupAndParentnode(String txtgroup, Integer parentnode);

    /** Создание List<DTOgroups> для выгрузки данных в Excel
     * @return
     */
    @Query(value = "select new com.pandev.dto.DTOgroups(g.ordernum, g.levelnum, s.txtgroup, g.txtgroup) " +
            "from Groups g join Groups s on g.parentnode = s.id " +
            "order by g.rootnode, g.ordernum")
    List<DTOgroups> findAllGroupsToDownload();

    /**
     * Выборка связанных записей для удаления элементов
     * @param groupid
     * @return
     */
    @Query(value = "select g from Groups g " +
            "where g.rootnode = (select r.rootnode from Groups r where r.id = :groupid) " +
            "and g.parentnode >= :groupid"  )
    List<Groups> selectGroupsForDelete(Integer groupid);

}
