import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class htmlunit {
	public static void main(String[] args) throws Exception {
		// Отключаем отображение ошибок
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

		String url = "";				// для тестовых нужд
		if (url.length() == 0 && args.length > 0) {
			url = args[0];				// читаем url из параметров в консоли
		}

		// Если есть URL, то запрашиваем снимок страницы (вместе с контентом, генерируемым ajax)
		if (url.length() > 0) {
			// Используем движок Firefox
			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
			// Нет необходимости обрабатывать CSS
			webClient.getOptions().setCssEnabled(false);
			// Ждем, пока отработают ajax-запросы, выполняемые при загрузке страницы
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			// Запрашиваем и рендерим веб-страницу
			HtmlPage page = webClient.getPage(url);
			// Выводим исходный код страницы в консоль
			System.out.println(page.asXml());
			// Закрываем headless-браузер, освобождаем память
			webClient.closeAllWindows();
		}
	}
}
