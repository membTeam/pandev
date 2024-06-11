package com.pandev.repositories;

import com.pandev.entities.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
