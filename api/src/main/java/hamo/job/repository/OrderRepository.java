package hamo.job.repository;

import hamo.job.entity.Order;
import hamo.job.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    int countByUser(User user);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.user = :user")
    double sumTotalAmountByUser(@Param("user") User user);
}
