package samoth69.plugin;

import jdk.vm.ci.meta.Local;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class GameRunningProcedure extends BukkitRunnable {
    private Main main;
    private StopWatch stopWatch = new StopWatch();
    private ArrayList<String> text = new ArrayList<>();

    public GameRunningProcedure(Main main) {
        this.main = main;
        //this.startTime = LocalTime.ofNanoOfDay(System.nanoTime());
        this.stopWatch.start();
    }

    @Override
    public void run() {
        //plus d'info ici: https://commons.apache.org/proper/commons-lang/javadocs/api-2.6/src-html/org/apache/commons/lang/time/FastDateFormat.html#line.534
        FastDateFormat fdfChrono = FastDateFormat.getInstance("kk:mm:ss");
        LocalDateTime ldtCurrent = LocalDateTime.now();
        LocalDateTime ldtChrono = LocalDateTime.parse(String.valueOf(stopWatch.getTime()), DateTimeFormatter.ofPattern("A"));

        text.clear();
        text.add(Main.startText + ChatColor.GOLD + "Durée: " + ChatColor.YELLOW + fdfChrono.format(stopWatch.getTime()));
        if (main.gameSettings.isEnableTaupe()) {
            Long dif = this.main.gameSettings.getTpsPVP() - stopWatch.getTime();
            text.add(Main.startText + ChatColor.GOLD + "Taupe: " + ChatColor.YELLOW + ldtChrono.format(DateTimeFormatter.ISO_LOCAL_TIME));
        }


        this.main.updateScoreboard(text);
    }

    private LocalTime dif(LocalTime start, LocalTime end) {
        int h, m, s;
        h = end.getHour() - start.getHour();
        m = end.getMinute() - start.getMinute();
        s = end.getSecond() - start.getSecond();

        if (m < 0) {

        }
        return 0;
    }
}
