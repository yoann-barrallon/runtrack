package fr.yoannbarrallon.runtrack.stat;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.stat.dto.OverallStatsResponse;
import fr.yoannbarrallon.runtrack.stat.dto.WeeklyStatsResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/overall")
    public OverallStatsResponse overall(@AuthenticationPrincipal User user) {
        return statService.overall(user.getId());
    }

    @GetMapping("/weekly")
    public List<WeeklyStatsResponse> weekly(@AuthenticationPrincipal User user) {
        return statService.weekly(user.getId());
    }
}
