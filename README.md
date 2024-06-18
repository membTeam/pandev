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

# Базовые сценарии взаимодействия и принципы
## действия пользователя регламентируются состоянием :
- AWAITING_NAME ожидания ввода имени, если клиент не зарегистрирован в БД. Включается на /start 
- AWAITING_REMOVE_ELEMENT ожидание ввода идентификатора удаляемого элемента  
- AWAITING_ADD_ELEMENT ожидание ввода идентификатора добавления корневого или субЭлемента
- NONE нейтральное состояние. В этом состоянии возможен ввод оговоренных команд

## ВСЕ команды ассоциированы с перечислением:
- COMD_REMOVE_ELEMENT -> /removeElement
- COMD_ADD_ELEMENT -> /addElement
- COMD_HELP -> /help
- COMD_VIEW_TREE -> /viewTree
- COMD_START -> /start
- COMD_STOP -> /stop
- COMD_CANCEL -> /cancel
## Текстовые сообщения (загружаются из соответствующих файлов):
- FILE_START_NOT_REGISTER_USER -> start-text.txt приглашение для не зарегистрированного пользователя
- FILE_START_REGISTER_USER -> start-register-user.txt приглашение для зарегистрированного пользователя
- FILE_ADD_ELEMENT -> comd_addelement.txt информационное сообщение для добавления элемента
- FILE_REMOVE_ELEMENT -> comd_removeelement.txt информационное сообщение для удаления элемента
- FILE_DEFAULT -> comd_empty.txt сообщение на случай не корректных действий пользователя

## События и сообщения telegramBot
- TelegramBot.replyToButtons   текстовые сообщения
- TelegramBot.replyToDocument  обработка Document
- util.ResponseHandl  Класс обработки ВСЕХ событий
  - replyToStart (/start) запускается через TelegramBot.Ability -> action(ctx -> responseHandl.replyToStart(ctx.chatId()))  
  - replyToDistributionMess менеджер входящих сообщений
  - replyToDocument менеджер обработки документов (Excel)
  - initMessage -> создание шаблона сообщений, используется по всему решению
  
### Основыне зависимости методов в util.ResponseHandl
- класс **ResponseHandl** создается через TelegramBot
- **replyToDistributionMess**
  - replyToCommand    
- **replyToMess** менеджер команд в зависимости от состояния   
  - replyToName -> AWAITING_NAME 
  - AWAITING_ADD_ELEMENT || AWAITING_REMOVE_ELEMENT -> addOrRemoveElement
  - replyToCommand -> обработка команд пользователя

  - sendMessageToUser -> вывод информационного сообщения и приглашения для команд: COMD_REMOVE_ELEMENT, COMD_ADD_ELEMENT
  - useTemplCommand -> ответное сообщение для команд: COMD_HELP, COMD_VIEW_TREE (сообщение создаестя на основе рефлексии) 
  - replyToCancel   -> отказ от ввода данных и переход на команду /help
- методы с префиксом get** для доступа к beanService из TelegramBot

### Сервисы
- **templCommand.CommService** сервис создания классов обработчиков на основе сообщений:
  COMD_REMOVE_ELEMENT, COMD_ADD_ELEMENT, COMD_REMOVE_ELEMENT.  

  По значению message.getMessage иницируется класс обработчик.  
  templCommand.Comd** шаблон классов обработчиков.   

  Все классы - реализация интерфейса TemplCommand  
  Используется из **ResponseHandl**   

 
### Утилиты (utils.**) 
- **FileAPI** сервис загрузки данных из файлов   
- **UserState** перечисление состояний 
- **Constanfs** константы
- **GroupsApi** заменен на ExcelService.saveGroupParentFromExcel 
- **utils.excelAPI.ExcelService** сервис загрузки excel файлов.    
  Можно использовать для массовой загрузки данных через excel файла 
- **InitListGroups** конвертор данных List List Object into List Groups
- **DTOresult**  класс обмена данных между методами. 


### Репозитории (repositories)
- GroupsRepository   
  особо сложные запросы: findAllGroupsBytxtGroup, findAllGroupsByParentId, maxOrdernum
  отмечены соответствующими комментариями.   
  Для этих запросов неизбежно использовались нативные запросы,  
  т.к. Hibernate поддерживает ограниченный набор функционала postgresql
  
  ВСЕ запросы покрыты модульными тестами.  
  ***ВНИМАНИЕ***: модульные тесты привязаны к состоянию БД при разработке  
- TelegramChatRepository 

#### Особенности структуры классов-сущностей БД
- TelegramChat регистрация пользователя
- Groups древовидная структура  
  rootnode id корневого узла  
  parentnode id родительского узла  
  ordernum индекс размещения узла в пределах корневого узла
  levelnum уровень вложенности   

Структура Groups содержит всю информацию по древовидной структуре.        
Добавление узла изменяет значения levelnum (сдвиг в право), а также у ordernum у всех нижележащих узлов, входящих в структуру своего корневого узла.       
Удаление узла изменяет значения только ordernum (сдвиг в лево)               
Используемая структура позволяет создавать и управлять древовидной структурой любой вложенности.  

При массовой загрузке из excel такая модификация осуществляется по каждому обрабатываемому элементу в контексте к используемому корневому узлу. 

### Загрузка данных из excel файла. 
- файл предназначенный для загрузки данных должен использовать определенный шаблон:  
  any-data/extenal-resource/test-excel.xlsx  
  На основе этого шаблона создается пользовательский варинт  

Основные методы: readFromExcel, saveDataByExcelToDb покрыты модульными тестами.  
saveGroupParentFromExcel & saveSubNodeFromExcel универсальные и используются для обработки через tekegramBot

**Пример вывода в telegramBot (* показатель вложенности)**   
Javascript  
*Webdeveloper  
**Тестировщики  
**Программисты  
Java  
*Джюниор  
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
161	1		1	160		160	джюниор   
162	1		2	160		160	мидл   
163	1		3	160		160	управленцы   
164	2		4	163		160	senior   
165	2		5	163		160	architect   
166	3		6	165		160	python   


#### Ограничения:
Строковые идентификаторы дочерних узлов д/быть уникальными.      
Для комбинации strParentNode strSubNode    
strParentNode если нет в БД -> создается корневой узел    
strSubNode если есть в БД ВСЯ обработка загрузки данных останавливается
