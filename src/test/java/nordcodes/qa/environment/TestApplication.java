package nordcodes.qa.environment;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class TestApplication implements AutoCloseable {
  private Process process;
  private Path log;
  private URI baseUrl;

  /**
   * Запускает приложение на свободном порту и ожидает его готовности.
   *
   * @param mockUrl адрес внешнего сервиса WireMock
   * @param apiKey ключ доступа к приложению
   * @throws Exception если приложение не удалось запустить
   */
  public void start(String mockUrl, String apiKey) throws Exception {
    Path jar =
        Path.of(System.getProperty("app.jar", "app/internal-0.0.1-SNAPSHOT.jar")).toAbsolutePath();
    if (!Files.isRegularFile(jar)) {
      throw new IllegalStateException("Не найден JAR приложения: " + jar);
    }
    Path directory = Path.of("target", "application-logs");
    Files.createDirectories(directory);
    log = Files.createTempFile(directory, "application-", ".log");
    String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
    Path java = Path.of(System.getProperty("java.home"), "bin", executable);
    process =
        new ProcessBuilder(
                java.toString(),
                "-Dsecret=" + apiKey,
                "-Dmock=" + mockUrl + "/",
                "-Dfile.encoding=UTF-8",
                "-jar",
                jar.toString(),
                "--server.port=0",
                "--server.address=127.0.0.1")
            .redirectErrorStream(true)
            .redirectOutput(log.toFile())
            .start();
    try {
      await().atMost(45, TimeUnit.SECONDS).until(this::isReady);
    } catch (Exception error) {
      close();
      throw new IllegalStateException("Не удалось запустить приложение:\n" + logText(), error);
    }
  }

  private boolean isReady() throws IOException {
    String output = Files.readString(log);
    if (!process.isAlive()) {
      throw new IllegalStateException("Приложение завершилось до готовности:\n" + output);
    }
    var port = Pattern.compile("Tomcat started on port (\\d+)").matcher(output);
    if (!port.find() || !output.contains("Started AqaApplication")) {
      return false;
    }
    baseUrl = URI.create("http://127.0.0.1:" + port.group(1));
    return true;
  }

  public URI baseUrl() {
    return baseUrl;
  }

  private String logText() {
    try {
      return log == null ? "Процесс еще не запускался" : Files.readString(log);
    } catch (IOException error) {
      return "Не удалось прочитать журнал: " + error.getMessage();
    }
  }

  @Override
  public void close() {
    if (process == null || !process.isAlive()) {
      return;
    }
    try {
      process.destroy();
      if (!process.waitFor(5, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
          throw new IllegalStateException("Не удалось остановить процесс приложения");
        }
      }
    } catch (InterruptedException error) {
      process.destroyForcibly();
      Thread.currentThread().interrupt();
    }
  }
}
