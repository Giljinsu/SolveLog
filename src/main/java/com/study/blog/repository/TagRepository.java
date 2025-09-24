package com.study.blog.repository;

import com.study.blog.entity.Tag;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long>, TagRepositoryCustom {
    @Query("select count(t.id) > 0 from Tag t where lower(t.name) = lower(:name)")
    Boolean existsByName(@Param("name") String name);

    List<Tag> findByNameInIgnoreCase(Collection<String> names);

    Tag findByNameIgnoreCase(String s);


}
