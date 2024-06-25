# Разработка Telegram-бота для управления деревом категорий на базе Spring Boot и PostgreSQL

## Цель: Создать Telegram-бота, который позволит пользователям создавать, просматривать и удалять дерево категорий
## Основные требования:
Проект должен быть реализован на Spring Boot.
- Для хранения информации о категориях используется база данных
  PostgreSQL.
- Документирование кода.
- Для работы с Telegram API рекомендуется использовать
  библиотеку TelegramBots.
- Для работы с базой данных можно использовать Spring Data JPA.
- Для обработки команд рекомендуется использовать шаблон
  проектирования Command.
- Загрузка, выгрузка данных из Excel  

## Критерии оценки:
- Корректность выполнения команд.
- Структура кода, наличие комментариев и документации.
- Обработка возможных исключений и ошибок ввода пользователя.
- Использование принципов ООП и шаблонов проектирования.

# Основные технологии и фреймворки:
- Spring Boot
- Postgresql
- hypersistence-utils-hibernate-62
- telegrambots-abilities
- org.apache.poi

# Используемые сервисы 

## ВСЕ команды ассоциированы с перечислением:
- COMD_REMOVE_ELEMENT -> /removeElement
- COMD_ADD_ELEMENT -> /addElement
- COMD_HELP -> /help
- COMD_VIEW_TREE -> /viewTree
- COMD_START -> /start
- COMD_DOWNLOAD -> /download 
- COMD_UPLOAD -> /upload

## Текстовые сообщения (загружаются из соответствующих файлов):
- FILE_START_NOT_REGISTER_USER -> start-text.txt приглашение для не зарегистрированного пользователя
- FILE_HELP -> comd_help.txt

## Обработка сообщений telegramBot
- /download from TelegramBot
- ВСЕ остальные запросы from ResponseController

### Сервисы
- **templCommand.CommService** сервис создания классов обработчиков на основе сообщений:
  COMD_VIEW_TREE, COMD_REMOVE_ELEMENT, COMD_ADD_ELEMENT

  По значению message.getMessage иницируется объект класса обработчика (templCommand.Comd**)  
  templCommand.templCommand интерфейс классов обработчиков.

### Утилиты (utils.**)
- **FileAPI** сервис загрузки данных из файлов
- **UserState** перечисление состояний
- **Constanfs** константы
- **utils.excelAPI.ExcelService** сервис загрузки excel файлов.  
  
  Используется для загрузки и выгрузки данных из Excel    
 
  Основные методы: readFromExcel, saveDataByExcelToDb покрыты модульными тестами (ExcelServiceTest). 
  
  для загрузки и выгрузки данные из Excel требуются специальные шаблоны:   
  any-data/extenal-resource/template.xlsx шаблон для download    
  any-data/extenal-resource/test-upload-excel.xlsx образец для upload    

------------- 

  Методы сервиса ExcelService универсальные: используются для добавления элемента 
  из telegramBot и загрузки начальных данных из com/pandev/configuration/LoadData  

- **InitListGroups** конвертор данных List List Object into List Groups
- **DTOresult**  класс обмена данных между методами.
- **InitListViewWithFormated** создание форматированного вывода treeView 


### Репозитории (repositories)
**При первом старте, если в БД нет данных, выполняется начальная загрузка: com/pandev/configuration/LoadData.java**
- GroupsRepository 
  Скрипты отмечены соответствующими комментариями.  
  Для скриптов, которые не поддерживаются Hibernate, использованы опции nativeQuery = true   

  ВСЕ запросы покрыты модульными тестами.  
  ***ВНИМАНИЕ***: модульные тесты привязаны к состоянию БД при разработке
- TelegramChatRepository

#### Особенности структуры классов-сущностей БД
- Groups древовидная структура  
  - rootnode id корневого узла  
  - parentnode id родительского узла  
  - ordernum индекс размещения узла в пределах корневого узла  
  - levelnum уровень вложенности
  - txtgroup  строковый идентификатор элемента (уникальные значения)

----------

Структура Groups содержит всю информацию по древовидной структуре.        
Добавление узла изменяет значения levelnum (сдвиг в право), а также у ordernum у всех нижележащих узлов, входящих в структуру своего корневого узла.       
Удаление узла изменяет значения только ordernum (сдвиг в лево)                   
Используемая структура позволяет создавать и управлять древовидной структурой любой вложенности.

### Загрузка из Excel:
- если нет корневого узла -> создается
- в БД добавляются только новые элементы  
- если выполнить дважды операцию upload состояние БД не изменится
- Шаблоны: см. выше в разделе Утилиты

  

**Пример вывода в telegramBot (Звездочка - показатель вложенности)**   
Javascript  
*Webdeveloper  
**Тестировщики  
**Программисты  
Java  
*Джуниор  
*Мидл  
*Управленцы  
**Senior  
**Architect  
***Python

**Структура в БД**  
**id  level order parent root txtgroup**         
156	0		0	156		156	javascript   
157	1		1	156		156	webdeveloper     
158	2		2	157		156	тестировщики   
159	2		3	157		156	программисты  
160	0		0	160		160	java   
161	1		1	160		160	джуниор   
162	1		2	160		160	мидл   
163	1		3	160		160	управленцы   
164	2		4	163		160	senior   
165	2		5	163		160	architect   
166	3		6	165		160	python
