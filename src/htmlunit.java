import java.util.regex.*;
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
			// Даем время выполниться setTimeout в javascript
			webClient.waitForBackgroundJavaScript(10000);
			// Запоминаем doctype
			Pattern doctypePattern = Pattern.compile("<!DOCTYPE((.|\n|\r)*?)>");
			String sourceHtml = page.getWebResponse().getContentAsString();
			Matcher doctypes = doctypePattern.matcher(sourceHtml);
			doctypes.find();
			String doctype = doctypes.group(0);
			// Заменяем doctype xml на исходный
			String resultXml = page.asXml();
			resultXml = resultXml.replaceAll("<\\?((.|\n|\r)*?)\\?>", doctype);
			// Убираем все теги script
			resultXml = resultXml.replaceAll("<script\\b[^<]*(?:(?!<\\/script>)<[^<]*)*<\\/script>", "");
			// Исправляем кривую замену тегов <i></i> на <i/>
			Pattern inlineTagsPattern = Pattern.compile("(<i\\s.*)\\/>");
			Matcher inlineTags = inlineTagsPattern.matcher(resultXml);
			if (inlineTags.find()) {
				resultXml = inlineTags.replaceAll("$1></i>");
			}
			// Выводим исходный код страницы в консоль
			System.out.println(resultXml);
			// Закрываем headless-браузер, освобождаем память
			webClient.closeAllWindows();
		}
	}
}
