package com.ankat.consumer.repository;

import com.ankat.consumer.entity.Trace;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitRepository extends CrudRepository<Trace, Long> { }
