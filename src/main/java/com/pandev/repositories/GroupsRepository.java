package com.pandev.repositories;

import com.pandev.entities.Groups;
import com.pandev.entities.GroupsDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.stream.LongStream;

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
            "where ordernum > (select coalesce(max(ordernum), (select max(ordernum) from groups)) " +
            "from groups where parentnode = (select id from groups where txtgroup = :txtgroup) ) " +
            "order by ordernum desc", nativeQuery = true)
    List<List<Object>> findAllGroupsBytxtGroup(String txtgroup);

    @Query("select g from Groups g where g.rootnode = :rootnode and g.ordernum >= :ordernum order by g.ordernum")
    List<Groups> findListGroupsByOrdernum(Integer rootnode, int ordernum);

    @Query("select g from Groups g where g.rootnode = :rootnode")
    List<Groups> findAllElementByRoorNode(Integer rootnode);

}
