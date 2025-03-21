package org.group2.comp313.kitchen_companion.repository;

import org.group2.comp313.kitchen_companion.domain.CodeBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodeBookRepository extends JpaRepository<CodeBook, Integer> , JpaSpecificationExecutor<CodeBook> {
  }