package bad;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Entity
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    @OneToMany(fetch = FetchType.LAZY)
    private List<OrderLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public List<OrderLine> getLines() { return lines; }
}

@Entity
class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    private int quantity;

    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
}

interface OrderRepository extends JpaRepository<Order, Long> {
}

@Service
class OrderListService {

    private final OrderRepository orderRepository;

    OrderListService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> findOrders() {
        return orderRepository.findAll();
    }
}

@RestController
class OrderListController {

    private final OrderListService orderListService;

    OrderListController(OrderListService orderListService) {
        this.orderListService = orderListService;
    }

    @GetMapping("/orders")
    public List<Order> list() {
        List<Order> orders = orderListService.findOrders();
        for (Order order : orders) {
            int total = 0;
            for (OrderLine line : order.getLines()) {
                total += line.getQuantity();
            }
            System.out.println(order.getCustomerName() + " : " + total + "点");
        }
        return orders;
    }
}
