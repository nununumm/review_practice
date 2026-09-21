package bad;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

enum OrderStatus {
    NEW, PAID, SHIPPED, CANCELED
}

@Entity
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private String shippingAddress;

    @Enumerated(EnumType.ORDINAL)
    private OrderStatus status;

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getShippingAddress() { return shippingAddress; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}

interface OrderRepository extends JpaRepository<Order, Long> {
}

@RestController
@Service
public class OrderStatusService {

    private final OrderRepository orderRepository;

    public OrderStatusService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping("/orders/{id}/status")
    public Order updateStatus(@PathVariable Long id, String newStatus) {
        Order order = orderRepository.findById(id).get();
        order.setStatus(OrderStatus.valueOf(newStatus));
        return orderRepository.save(order);
    }

    @GetMapping("/orders")
    public List<Order> list() {
        return orderRepository.findAll();
    }
}
