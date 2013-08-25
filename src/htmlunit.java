import java.util.*;
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
			Pattern doctypePattern = Pattern.compile("<!DOCTYPE((.|\n|\r)*?)>", Pattern.CASE_INSENSITIVE);
			String sourceHtml = page.getWebResponse().getContentAsString();
			Matcher doctypes = doctypePattern.matcher(sourceHtml);
			String doctype = "";
			if (doctypes.find()) {
				doctype = doctypes.group(0);
			}
			
			// Получаем xml запрашиваемой страницы
			String resultXml = page.asXml();
			
			// Заменяем doctype xml на исходный
			if (doctype.length() > 0) {
				resultXml = resultXml.replaceAll("<\\?((.|\n|\r)*?)\\?>", doctype);
			}
			
			// Убираем все теги script
			resultXml = resultXml.replaceAll("<script\\b[^<]*(?:(?!<\\/script>)<[^<]*)*<\\/script>", "");
			
			// Исправляем кривую замену строчных тегов в xml
			List<String> allMatches = new ArrayList<String>();
			List<String> allMatchesTagNames = new ArrayList<String>();
			List<String> inlineClosedTagsList = Arrays.asList("b", "big", "i", "small", "tt", "abbr", "acronym", 
					"cite", "code", "dfn", "em", "kbd", "strong", "samp", "var", "a", "bdo", "map", "object", 
					"q", "script", "span", "sub", "sup", "button", "label", "select", "textarea");
			Pattern inlineTagsPattern = Pattern.compile("<([a-z]+)\\s.*\\/>");
			Matcher inlineTags = inlineTagsPattern.matcher(resultXml);
			
			while (inlineTags.find()) {
				if (inlineClosedTagsList.contains(inlineTags.group(1))) {
					allMatches.add(inlineTags.group());
					allMatchesTagNames.add(inlineTags.group(1));
				}
			}
			
			for (int i = 0; i < allMatches.size(); i++) {
				String replacementTag = allMatches.get(i);
				int replacementTagLength = replacementTag.length();
				String fixedTag = new StringBuilder(replacementTag.substring(0, replacementTagLength - 2))
								  .append("></").append(allMatchesTagNames.get(i)).append(">").toString();
				resultXml = resultXml.replace(replacementTag, fixedTag);
			}
			
			// Выводим исходный код страницы в консоль
			System.out.println(resultXml);
			
			// Закрываем headless-браузер, освобождаем память
			webClient.closeAllWindows();
		}
	}
}
