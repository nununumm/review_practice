@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserListController {
    private final UserRepository userRepository;

    @GetMapping("/with-post-count")
    public ResponseEntity<List<UserResponseDto>> getUsersWithPostCount() {
        List<User> users = userRepository.findAll(); // ① 全件取得
        List<UserResponseDto> responseList = new ArrayList<>();
        
        for (User user : users) {
            UserResponseDto dto = new UserResponseDto();
            dto.setUsername(user.getUsername());
            // ❌ ループのたびにSQLが発行される！（N+1問題）
            dto.setPostCount(user.getPosts().size()); 
            responseList.add(dto);
        }
        return ResponseEntity.ok(responseList);
    }
}