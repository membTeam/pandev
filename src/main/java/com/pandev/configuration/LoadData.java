package com.pandev.configuration;

import com.pandev.repositories.GroupsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

/**
 * Загрузка начальных данных
 */
public class LoadData implements CommandLineRunner {

    @Autowired
    private GroupsRepository groupsRepo;

    @Override
    public void run(String... args) throws Exception {
        if (groupsRepo.isExistsData()) {
            return;
        }


    }
}
