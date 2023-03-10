package com.rick.demo.module.project.dao;

import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.demo.module.project.domain.entity.Film;
import org.springframework.stereotype.Repository;


/**
 * @author Rick
 * @createdAt 2022-10-29 21:33:00
 */

@Repository
public class FilmDAO extends EntityDAOImpl<Film, String> {

}