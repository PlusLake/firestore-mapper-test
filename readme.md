Firestore's timestamp only accept java.sql.Timestamp or java.util.Date (See https://github.com/FasterXML/jackson-databind/issues/2983)<br>
However those classes are deprecated since Java8.<br>
So this is a sample to convert a object to a Firestore-uploadable-object with reflection
