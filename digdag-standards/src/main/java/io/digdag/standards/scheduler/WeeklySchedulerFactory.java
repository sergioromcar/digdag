package io.digdag.standards.scheduler;

import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigException;
import io.digdag.spi.Scheduler;
import io.digdag.spi.SchedulerFactory;
import it.sauronsoftware.cron4j.SchedulingPattern;

import java.time.ZoneId;

import static io.digdag.standards.scheduler.DailySchedulerFactory.parseAt;

public class WeeklySchedulerFactory
        implements SchedulerFactory {
    @Override
    public String getType() {
        return "weekly";
    }

    @Override
    public Scheduler newScheduler(Config config, ZoneId timeZone) {
        String desc = config.getOptional("_command", String.class).or(() -> config.get("at", String.class));

        String[] fragments = desc.split(",", 2);
        if (fragments.length != 2) {
            throw new ConfigException("weekly>: scheduler requires day,hh:mm:ss format: " + desc);
        }

        String day = fragments[0].trim();
        String time = fragments[1].trim();

        // cron4j incorrectly parses negative day of week as positive.
        try {
            int dayNumber = Integer.parseInt(day);
            if (dayNumber < 0) {
                throw new ConfigException("weekly>: invalid day: " + day);
            }
        } catch (NumberFormatException ignore) {
            // Not an integer, validate using SchedulingPattern.validate.
        }

        long dailyDelay = parseAt("weekly>", time);

        String cronPattern = "0 0 * * " + day;
        boolean valid = SchedulingPattern.validate(cronPattern);
        if (!valid) {
            throw new ConfigException("weekly>: scheduler requires day,hh:mm:ss format: " + desc);
        }

        return new CronScheduler(cronPattern, timeZone, dailyDelay);
    }
}
