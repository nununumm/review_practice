// NG: コントローラーに処理を書きすぎている＆パスワード平文保存
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
        User existingUser = userRepository.findByEmail(userDto.getEmail());
        if (existingUser != null) return ResponseEntity.badRequest().body("Error");

        User newUser = new User();
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(userDto.getPassword()); // NG 平文保存！
        userRepository.save(newUser);
        
        return ResponseEntity.ok("Success");
    }
}