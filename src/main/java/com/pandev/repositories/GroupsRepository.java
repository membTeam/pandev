package com.pandev.repositories;

import com.pandev.entities.Groups;
import com.pandev.entities.GroupsDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Репозиторий бизнес модели Groups
 */
public interface GroupsRepository extends JpaRepository<Groups, Integer> {

    /**
     * Верификация наличия записей в таблице.
     * Используется перед начальной загрузкой данных
     * @return
     */
    @Query(value = "select exists(select * from groups)",nativeQuery = true)
    boolean isExistsData();

    @Query(value = "select new com.pandev.entities.GroupsDetails(g.levelnum, g.txtgroup) from Groups g order by g.rootnode, g.ordernum")
    List<GroupsDetails> getTreeData();

    Groups findByTxtgroup(String txtgroup );

    @Query(value = "select * from groups where id = (select coalesce(min(id), -1) from groups)", nativeQuery = true)
    Groups firstElement();

    /**
     * Выборка строк таблицы в которых будут обновляться значения ordernum.
     * В итоге все эти элементы будут смещены по структуре отностельно корневого элемента
     * Используется при добавлении субЭлементов
     * @param txtgroup строковый идентификатор узла. Значения уникальные на уровне таблицы
     * @return
     */
    @Query(value = "select id, rootnode, parentnode, txtgroup, ordernum, levelnum from groups " +
            "where ordernum > (select coalesce(max(ordernum), 2147483640) " +
            "from groups where parentnode = (select id from groups where txtgroup = lower(trim(:txtgroup)) ) ) " +
            "and rootnode = :rootId order by ordernum desc", nativeQuery = true)
    List<List<Object>> findAllRowsAfterCurrentStruct(String txtgroup, Integer rootId);

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
     * Выборка ВСЕХ записей, связанных через parentnode and ordernum.
     * Используется при удаления элемента
     * @param parentnode удаляемый узел
     * @return
     */
    @Query(value = "select * from groups " +
            "where rootnode = (select rootnode from groups where id = :parentnode ) " +
            "and ordernum in ( select ordernum from groups where parentnode = :parentnode )", nativeQuery = true)
    List<Groups> findAllGroupsByParentIdExt(Integer parentnode);

    /**
     * Все записи расположенные ниже удаляемой в контексте корневого узла,
     * обновляются по полю ordernum.
     * Используется после удаления узла.
     * Запускается после обработки скрипта findAllGroupsByParentIdExt
     * @param rootNode удаляемый узел
     * @return
     */
    @Query(value = "select * from groups where rootNode = :rootNode and ordernum > 0", nativeQuery = true)
    List<Groups> findAllGroupsForUpdateOrdernum(Integer rootNode);

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
            "end max_order_num", nativeQuery = true)
    Integer maxOrdernum(Integer rootnode, Integer parentnode);

    @Query("select g from Groups g where g.rootnode = :rootnode and g.ordernum > :ordernum order by g.ordernum")
    List<Groups> findListGroupsByOrdernum(Integer rootnode, int ordernum);

    @Query("select g from Groups g where g.rootnode = :rootnode")
    List<Groups> findAllElementByRootNode(Integer rootnode);

    List<Groups> findAllByTxtgroupIn(List<String> ls);

    List<Groups> findAllByParentnodeInAndOrdernumNot(List<Integer> ls, int ordernum);

    @Query(value = "select exists(select * from groups where txtgroup = lower(trim(:txtgroup)) and parentnode = :parentnode)", nativeQuery = true)
    boolean isExistsBytxtgroupAndParentnode(String txtgroup, Integer parentnode);







}
