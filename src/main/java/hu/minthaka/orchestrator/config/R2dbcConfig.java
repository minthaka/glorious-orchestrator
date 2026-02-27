package hu.minthaka.orchestrator.config;

import hu.minthaka.common.enums.Command;
import hu.minthaka.common.enums.Status;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.codec.EnumCodec;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.EnumWriteSupport;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig {

  @Bean
  public ConnectionFactory connectionFactory() {
    return new PostgresqlConnectionFactory(
        PostgresqlConnectionConfiguration.builder()
            .host("localhost")
            .port(5333)
            .database("healthcare")
            .username("doki")
            .password("doki")
            .codecRegistrar(EnumCodec.builder()
                .withEnum("status", Status.class)
                .withEnum("command", Command.class)
                .build())
            .build()
    );
  }

  @Bean
  ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
    return new R2dbcTransactionManager(connectionFactory);
  }

  @Bean
  public R2dbcCustomConversions r2dbcCustomConversions() {
    List<Object> converters = new ArrayList<>();

    // Enum Writing Converters (Using EnumWriteSupport for Postgres Native Types)
    converters.add(new StatusWritingConverter());
    converters.add(new CommandWritingConverter());

    return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
  }

  // --- Status Converters ---

  @WritingConverter
  public static class StatusWritingConverter extends EnumWriteSupport<Status> {
    // This tells Spring Data to pass the Enum to the R2DBC Driver (Codec)
    // instead of converting it to a String automatically.
  }

  // --- Command Converters ---

  @WritingConverter
  public static class CommandWritingConverter extends EnumWriteSupport<Command> {
  }

}
