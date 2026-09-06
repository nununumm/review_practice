@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseApprovalService expenseApprovalService;
    private final ExpenseRepository expenseRepository;

    /** 経費申請の詳細を表示する */
    @GetMapping("/{expenseId}")
    public Expense getExpense(@PathVariable Long expenseId) {
        return expenseRepository.findById(expenseId).get();
    }

    /** 申請一覧（画面の「申請一覧」タブから呼ばれる） */
    @GetMapping
    public List<Expense> list() {
        return expenseRepository.findAll();
    }

    /** 経費申請を承認する。承認者のIDは画面から送られてくる */
    @PostMapping("/{expenseId}/approve")
    public String approve(@PathVariable Long expenseId,
                          @RequestParam Long approverId,
                          @RequestParam(required = false) String comment) {
        return expenseApprovalService.approve(expenseId, approverId, comment);
    }
}
