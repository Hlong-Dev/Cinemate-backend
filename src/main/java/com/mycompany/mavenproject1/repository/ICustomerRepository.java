package com.mycompany.mavenproject1.repository;

import com.mycompany.mavenproject1.model.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICustomerRepository extends JpaRepository<Customers,Long> {

}