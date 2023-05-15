# CrackHash
## Предисловие
Мы стремимся реализовать распределенную систему для взлома хэша под кодовым именем CrackHash. Непосредственно взлом хэша будем реализовывать через простой перебор словаря сгенерированного на основе алфавита (brute-force). В общих чертах система должна работать по следующей логике:
В рамках системы существует менеджер, который принимает от пользователя запрос, содержащий MD-5 хэш некоторого слова, а также его максимальную длину.
Менеджер обрабатывает запрос: генерирует задачи в соответствии с заданным числом воркеров (вычислительных узлов) на перебор слов составленных из переданного им алфавита. После чего отправляет их на исполнение воркерам через очередь RabbitMq. 
Каждый воркер принимает задачу, вычисляет собственный диапазон в котором нужно проверять слова, генерирует и вычисляет их хэш. Находит слова у которых он совпадает, и результат работы возвращает менеджеру через очередь.

# Task 1.  Services Implementation
В рамках первой лабораторной работы необходимо реализовать приложения менеджера и воркера, а также организовать простое их взаимодействие через HTTP.

### Примечание:
Настройка очередей и producer-ов/listener-ов для них в рамках данной задачи не предполагается!

## Общие требования к сервисам:
Реализация приложений предполагается на языке Java (11+) с использованием  фреймворка Spring Boot.
Для сборки рекомендуется использовать Gradle (6+).
Для развертывания сервисов необходимо использовать docker-compose, конфигурация с одним воркером. 
Запросы между менеджером и воркером необходимо передавать внутри сети docker-compose по протоколу HTTP, используя в качестве доменов имена сервисов.

## Требования к менеджеру
Менеджер должен предоставлять клиенту REST API в формате JSON для взаимодействия с ним.

Запроса на взлом хэша (слово abcd):
POST /api/hash/crack
Request body:
{
    "hash":"e2fc714c4727ee9395f324cd2e7f331f", 
    "maxLength": 4
}

В ответ менеджер должен отдавать клиенту идентификатор запроса, по которому тот сможет обратится за получением ответа.
Response body:
{
    "requestId":"730a04e6-4de9-41f9-9d5b-53b88b17afac"
}

Для получения результатов менеджер должен представлять следующее API.

GET /api/hash/status?requestId=730a04e6-4de9-41f9-9d5b-53b88b17afac

Ответ,  если запрос еще обрабатывается.
Response body:
{
    "status":"IN_PROGRESS",
    "data": null
}

Ответ,  если ответ готов.
Response body:
{
   "status":"READY",
   "data": ["abcd"]
}

В качестве алфавита менеджер должен использовать строчные латинские буквы и цифры (ограничимся ими в целях экономии времени на вычисления).
Взаимодействие между менеджером и воркерами должно быть организовано в формате XML. Поэтому необходимо сгенерировать модель запроса менеджера к воркеру на основе xsd-схемы. Далее запросы для воркеров заполнять в соответствии с моделью.
Перед отправкой задач воркерам менеджер должен сохранить в оперативной памяти информацию о них с привязкой к запросу клиента в статусе IN_PROGRESS под идентификатором, который после должен быть выдан пользователю. При получении ответов от всех воркеров менеджер должен перевести статус запроса в READY. По истечению таймаута запрос должен перевестись в статус ERROR.
Для работы с состоянием запросов использовать потокобезопасные коллекции.
Взаимодействие с воркером организовать по протоколу HTTP с помощью Rest Template. Для этого в воркере необходимо реализовать контроллер для обработки запросов от менеджера, принимающий запрос по следующему пути:
POST /internal/api/worker/hash/crack/task


## Требования к воркерам:
Взаимодействие между воркером и менеджером также должно быть организовано в формате XML. Поэтому необходимо сгенерировать модель запроса ответа воркера на основе xsd-схемы. Ответ для менеджера заполнять в соответствии с моделью.
Взаимодействие с менеджером организовать по протоколу HTTP с помощью Rest Template. Для этого в менеджере необходимо реализовать контроллер для обработки ответов от воркера по следующему пути:
PATCH /internal/api/manager/hash/crack/request


Для генерации слов на основе полученного алфавита можно использовать библиотеку combinatoricslib. Она позволяет генерировать перестановки с повторениями заданной длины на основе заданного множества. Ключевое условие здесь, чтобы воркер не держал в памяти все сгенерированные комбинации т.к. при увеличении максимальной длины последовательности их банально станет очень много. Для этого библиотека предоставляет итератор по множеству комбинаций.
Расчет диапазона слов необходимо производить на основе значений PartNumber и PartCount из запроса от менеджера. Необходимо поделить всё пространство слов поровну между всеми воркерами.

# Task 2.  Fault tolerance
В рамках второй лабораторной работы необходимо модифицировать систему таким образом, чтобы обеспечить гарантированную обработку запроса пользователя (если доступен менеджер) т.е. обеспечить отказоустойчивость системы в целом.

## Основные требования:
Обеспечить сохранность данных при отказе работы менеджера
Для этого необходимо обеспечить хранение данных об обрабатываемых запросах в базе данных
Также необходимо организовать взаимодействие воркеров с менеджером через очередь RabbitMQ
Для этого достаточно настроить очередь с direct exchange-ем
Если менеджер недоступен, то сообщения должны сохраняться в очереди до момента возобновления его работы
RabbitMQ также необходимо разместить в окружении docker-compose
Обеспечить частичную отказоустойчивость базы данных
База данных также должна быть отказоустойчивой, для этого требуется реализовать простое реплицирование для нереляционной базы MongoDB
Достаточно будет схемы с одной primary и одной secondary нодой
Минимально рабочая схема одна primary нода, две secondary
Менеджер должен отвечать клиенту, что задача принята в работу только после того, как она была успешно сохранена в базу данных и отреплицирована
Обеспечить сохранность данных при отказе работы воркера(-ов)
В docker-compose необходимо разместить, как минимум, 2 воркера
Организовать взаимодействие менеджера с воркерами через очередь RabbitMQ (вторая, отдельная очередь), аналогично настроить direct exchange
В случае, если любой из воркеров при работе над задачей ”cломался” и не отдал ответ, то задача должна быть переотправлена другому воркеру, для этого необходимо корректно настроить механизм acknowledgement-ов
Если на момент создания задач нет доступных воркеров, то сообщения должны дождаться их появления в очереди, а затем отправлены на исполнение
Обеспечить сохранность данных при отказе работы очереди
Если менеджер не может отправить задачи в очередь, то он должен сохранить их у себя в базе данных до момента восстановления доступности очереди, после чего снова отправить накопившиеся задачи
Очередь не должна терять сообщения при рестарте (или падении из-за ошибки), для этого все сообщения должны быть персистентными (это регулируется при их отправке)

## Кейсы, которые будут проверяться:
стоп сервиса менеджера в docker-compose 
полученные ранее ответы от воркеров должны быть сохранены в базу и не должны потеряться
не дошедшие до менеджера ответы на задачи не должны потеряться, менеджер должен подобрать их при рестарте
стоп primary ноды реплик-сета MongoDB в docker-compose 
primary нода должна измениться, в система продолжать работу в штатном режиме
стоп RabbitMQ в docker-compose
все необработанные, на момент выключения очереди, сообщения после рестарта не должны потеряться
стоп воркера во время обработки задачи
сообщение должно быть переотправлено другому воркеру, задача не должна быть потеряна

### Примечания.
Для отправки и получения сообщений в формате xml в очередь лучше всего использовать MarshallingMessageConverter + Jaxb2Marshaller в связке с AmqpTemplate(отправка) и @RabbitListener (получение) 
При рестарте RabbitMQ сбрасывает статус отправленных сообщений (персистентных) из unacked в ready. Поэтому, например, допускается повторная обработка одной задачи двумя воркерами, что нужно учесть в логике менеджера.


# Полезные ссылки:
* Пример базового сервиса на Spring https://spring-projects.ru/guides/rest-service/
* База про XSD https://www.codeguru.com/java/xsd-tutorial-xml-schemas-for-beginners/
* XSD спецификация https://www.w3.org/TR/xmlschema11-1/
* Пример генерации JAXB моделей на основе xsd https://spring.io/guides/gs/producing-web-service/
* Генерация последовательностейhttps://github.com/dpaukov/combinatoricslib
* Rest Template https://docs.spring.io/spring-android/docs/current/reference/html/rest-template.html https://www.baeldung.com/rest-template
* JAXB + Rest Template https://stackoverflow.com/questions/41288036/how-do-i-use-jaxb-annotations-with-spring-resttemplate
* Гайд по RabbitMQ Exchanges https://habr.com/ru/post/489086/
* Деплой RabbitMQ в docker-compose https://www.section.io/engineering-education/dockerize-a-rabbitmq-instance/
* Дока по реплицированию MongoDB https://www.mongodb.com/docs/manual/replication/
* Гайд по настройке реплицирования MongoDB в окружении docker-compose https://flowygo.com/en/blog/mongodb-and-docker-how-to-create-and-configure-a-replica-set/
* RabbitMQ Consumer Acknowledgements and Publisher Confirms https://www.rabbitmq.com/confirms.html
* RabbitMQ Persistence Configuration https://www.rabbitmq.com/persistence-conf.html
