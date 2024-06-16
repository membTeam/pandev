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

    @Query(value = "select new com.pandev.entities.GroupsDetails(g.levelnum, g.txtgroup) from Groups g order by g.ordernum")
    List<GroupsDetails> getTreeData();

    Groups findByTxtgroup(String txtName );

    @Query(value = "select * from groups where id = (select coalesce(min(id), -1) from groups)", nativeQuery = true)
    Groups firstElement();

    /**
     * Выборка строк таблицы в которых будут обновляться значения ordernum.
     * В итоге все эти элементы будут смещены по структуре отностельно корневого элемента
     * Используется при добавлении субЭлементов
     * @param txtgroup
     * @return
     */
    @Query(value = "select id, rootnode, parentnode, txtgroup, ordernum, levelnum from groups " +
            "where ordernum > (select coalesce(max(ordernum), 2147483640) " +
            "from groups where parentnode = (select id from groups where txtgroup = :txtgroup) ) " +
            "order by ordernum desc", nativeQuery = true)
    List<List<Object>> findAllGroupsBytxtGroup(String txtgroup);


    /**
     * Максимальное значение ordernum из дочерних записей.
     * Если нет данных тогда значение ordernum самого родительского объекта
     * @param rootnode корневой узел
     * @param parentnode родительский узел
     * @return
     */
    @Query(value = "select case " +
            "when exists(Select * from groups where rootnode = :rootnode and parentnode=:parentnode) " +
            "then (select max(ordernum) from groups where rootnode = :rootnode and parentnode=:parentnode) " +
            "else (select ordernum from groups where id = :parentnode) " +
            "end max_order_num", nativeQuery = true)
    Integer maxOrdernum(Integer rootnode, Integer parentnode);

    @Query("select g from Groups g where g.rootnode = :rootnode and g.ordernum > :ordernum order by g.ordernum")
    List<Groups> findListGroupsByOrdernum(Integer rootnode, int ordernum);

    @Query("select g from Groups g where g.rootnode = :rootnode")
    List<Groups> findAllElementByRootNode(Integer rootnode);

}
