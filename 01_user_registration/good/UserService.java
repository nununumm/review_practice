@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // DIで注入される

    @Transactional
    public void register(UserDto userDto) {
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());
        
        User newUser = new User();
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(hashedPassword); // ⭕️ ハッシュ化して保存
        userRepository.save(newUser);
    }
}