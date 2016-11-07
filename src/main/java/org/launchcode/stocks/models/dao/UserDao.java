package org.launchcode.stocks.models.dao;

import javax.transaction.Transactional;

import org.launchcode.stocks.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by cbay on 5/10/15.
 */
@Transactional
@Repository
public interface UserDao extends CrudRepository<User, Integer> {

    User findByUserName(String userName);

    User findByUid(int uid);

}
