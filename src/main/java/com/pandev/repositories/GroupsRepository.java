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


    /*@Query(value = "select g.levelnum, g.txtgroup from Groups g order by g.ordernum")
    List<List<Object>> getTreeData();*/

}
