@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserListController {
    private final UserRepository userRepository;

    @GetMapping("/with-post-count")
    public ResponseEntity<List<UserPostCountDto>> getUsersWithPostCount() {
        // ⭕️ ループ処理なし！1発で集計済みのリストを受け取るだけ
        List<UserPostCountDto> responseList = userRepository.findAllWithPostCount();
        return ResponseEntity.ok(responseList);
    }
}