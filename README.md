# Document Q&A Assistant

An intelligent document question-answering system built with **Spring Boot** and **Spring AI**. Upload PDFs or text documents and get AI-powered answers to questions about their content.

## 🎯 Features

- **Document Upload**: Support for PDF and text file uploads
- **Intelligent Q&A**: Ask questions about document content using GPT-5.4-mini
- **Document Summary**: Auto-generate summaries of uploaded documents
- **Topic Extraction**: Extract key topics from documents
- **RESTful API**: Clean, easy-to-use API endpoints
- **Conversation History**: Support for multi-turn conversations

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- OpenAI API key (get it from [platform.openai.com](https://platform.openai.com))

### Installation

1. **Clone/Create Project**
   ```bash
   mkdir document-qa-assistant
   cd document-qa-assistant
   ```

2. **Set OpenAI API Key**
   ```bash
   export OPENAI_API_KEY="sk-your-api-key-here"
   ```

3. **Run Application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   Application will start at `http://localhost:8081`

## 📚 API Endpoints

### 1. Upload Document
```bash
curl -X POST http://localhost:8081/api/documents/upload \
  -F "file=@document.pdf"
```

**Response:**
```json
{
  "id": "uuid-here",
  "filename": "document.pdf",
  "content": "...",
  "uploadedAt": "2024-04-19T10:30:00",
  "pageCount": 5
}
```

### 2. Ask Question About Document
```bash
curl -X POST http://localhost:8081/api/documents/{documentId}/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the main topic of this document?",
    "documentId": "uuid-here"
  }'
```

**Response:**
```json
{
  "question": "What is the main topic?",
  "answer": "The document discusses...",
  "documentId": "uuid-here",
  "confidence": 0.95
}
```

### 3. Get Document Summary
```bash
curl http://localhost:8081/api/documents/{documentId}/summary
```

### 4. Extract Topics
```bash
curl http://localhost:8081/api/documents/{documentId}/topics
```

### 5. List All Documents
```bash
curl http://localhost:8081/api/documents
```

### 6. Delete Document
```bash
curl -X DELETE http://localhost:8081/api/documents/{documentId}
```

## 💡 Usage Examples

### Python Client
```python
import requests

BASE_URL = "http://localhost:8081/api/documents"

# Upload document
with open("research_paper.pdf", "rb") as f:
    response = requests.post(
        f"{BASE_URL}/upload",
        files={"file": f}
    )
    doc_id = response.json()["id"]

# Ask question
question_response = requests.post(
    f"{BASE_URL}/{doc_id}/ask",
    json={
        "question": "What are the key findings?",
        "documentId": doc_id
    }
)

print(question_response.json()["answer"])

# Get summary
summary = requests.get(f"{BASE_URL}/{doc_id}/summary").json()
print(summary["summary"])
```

### JavaScript/Node.js
```javascript
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');

const BASE_URL = 'http://localhost:8081/api/documents';

async function askQuestion(documentId, question) {
  const response = await axios.post(
    `${BASE_URL}/${documentId}/ask`,
    { question, documentId }
  );
  return response.data.answer;
}

// Usage
const answer = await askQuestion('doc-id', 'What is this about?');
console.log(answer);
```

## 🏗️ Project Structure

```
document-qa-assistant/
├── src/main/java/com/example/documentqa/
│   ├── DocumentQAApplication.java      # Main entry point
│   ├── controller/
│   │   └── DocumentController.java     # REST endpoints
│   ├── service/
│   │   ├── DocumentParsingService.java # PDF/text parsing
│   │   └── DocumentAIService.java      # AI Q&A logic
│   └── model/
│       └── Models.java                 # Data models
├── pom.xml                             # Maven configuration
└── application.properties              # Spring config
```

## 🔧 Configuration

Edit `application.properties` to customize:

```properties
# AI Model
spring.ai.openai.chat.options.model=gpt-4-turbo-preview

# Temperature (0-1, higher = more creative)
spring.ai.openai.chat.options.temperature=0.7

# File size limits
spring.servlet.multipart.max-file-size=10MB
```

## 🌟 Advanced Features

### Text Chunking
For large documents, the service automatically chunks text to optimize processing:
```java
String[] chunks = parsingService.chunkText(content, 5000);
```

### Multi-turn Conversations
Continue conversations with context:
```java
List<String> history = Arrays.asList(
  "What is AI?",
  "When was machine learning invented?"
);
String answer = aiService.continueConversation(content, history);
```

### Error Handling
- Returns 400 for invalid uploads
- Returns 404 for missing documents
- Returns 500 for API errors with clear messages

## 📊 Performance Tips

1. **Large Documents**: Use document chunking for better processing
2. **Multiple Queries**: Cache document summaries to reduce API calls
3. **Rate Limiting**: Add request throttling for production use

## 🔒 Production Considerations

1. **API Key Security**: Use environment variables or vault systems
2. **Database**: Replace in-memory DocumentStore with persistent storage
3. **Authentication**: Add Spring Security for API protection
4. **Rate Limiting**: Implement throttling to prevent abuse
5. **Document Storage**: Store files on disk/cloud instead of memory
6. **Logging**: Configure proper logging framework
7. **Testing**: Add unit and integration tests

## 📈 Future Enhancements

- [ ] Vector embeddings for semantic search
- [ ] Long-term memory with database
- [ ] Multi-model support (Anthropic Claude, etc.)
- [ ] Document versioning
- [ ] Real-time streaming responses
- [ ] Web UI dashboard
- [ ] Document format support (DOCX, XLS, etc.)
- [ ] Custom prompt templates

## 🤝 Contributing

Feel free to extend this project:
- Add new AI capabilities
- Improve document parsing
- Create a web UI
- Add database integration

## 📝 License

MIT License - feel free to use in your projects!

## 🆘 Troubleshooting

**Issue**: API key not found
```
Solution: export OPENAI_API_KEY="your-key" before running
```

**Issue**: PDF parsing fails
```
Solution: Ensure PDFBox dependency is in pom.xml
```

**Issue**: Out of memory with large documents
```
Solution: Increase JVM memory: mvn spring-boot:run -Dspring-boot.run.arguments="--Xmx2g"
```

## 📚 Resources

- [Spring AI Documentation](https://spring.io/projects/spring-ai)
- [OpenAI API Docs](https://platform.openai.com/docs)
- [Spring Boot Guide](https://spring.io/guides/gs/spring-boot/)
- [Apache PDFBox](https://pdfbox.apache.org/)

---

Built with ❤️ using Spring Boot and Spring AI
