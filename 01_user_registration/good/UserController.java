@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor // DI（依存性の注入）のためのLombokアノテーション
public class UserController {

    // ⭕️ DIを使って、Springくんに UserService を注入してもらう
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserDto userDto) {
        
        // ⭕️ ビジネスロジック（DB保存やパスワードハッシュ化）は全てServiceに委譲！
        userService.register(userDto);
        
        return ResponseEntity.ok("User registered successfully");
    }
}