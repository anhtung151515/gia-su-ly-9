<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.giasuly9.util.AiService" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>AI giải bài | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <script src="https://cdn.jsdelivr.net/npm/marked@12.0.2/marked.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/md-render.js"></script>
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container ai-shell">
        <div class="page-head">
            <h2 class="page-title"><i class="bi bi-stars text-primary"></i> AI giải bài</h2>
            <div class="page-sub">
                Hỏi bất kỳ bài nào thuộc kiến thức phổ thông. AI sẽ trả lời đầy đủ, từng bước.
            </div>
        </div>

        <%
            AiService ai = new AiService();
            com.giasuly9.model.User currentUser = (com.giasuly9.model.User) session.getAttribute("user");
            boolean isAdmin = currentUser != null && currentUser.isAdmin();
            if (!ai.isConfigured()) {
        %>
        <div class="alert alert-warning d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle-fill"></i>
            <div>
                <strong>Hệ thống AI chưa được cấu hình.</strong><br>
                Quản trị viên cần dán API key tại trang
                <a href="${pageContext.request.contextPath}/admin/ai-settings" class="fw-semibold">Cấu hình API AI</a>
                (lấy key miễn phí tại <a href="https://aistudio.google.com/apikey" target="_blank" rel="noopener">aistudio.google.com</a>).
            </div>
        </div>
        <% if (isAdmin) { %>
        <div class="card mb-3">
            <div class="card-head">
                <h5 class="card-title"><i class="bi bi-key"></i> Dán API key Gemini để kích hoạt AI ngay</h5>
            </div>
            <div class="card-body">
                <form id="keyForm" class="row g-2 align-items-end">
                    <div class="col-md-7">
                        <label class="form-label">Google AI Studio API key</label>
                        <input type="password" name="apiKey" id="apiKeyInput" class="form-control"
                               placeholder="AIzaSy..." autocomplete="off">
                        <div class="form-hint">
                            Lấy key miễn phí tại
                            <a href="https://aistudio.google.com/apikey" target="_blank" rel="noopener">aistudio.google.com/apikey</a>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Model</label>
                        <input type="text" name="model" id="modelInput" class="form-control" value="gemini-3.5-flash">
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary w-100" id="keyBtn">
                            <i class="bi bi-check-lg"></i> Kích hoạt
                        </button>
                    </div>
                </form>
                <div id="keyMsg" class="mt-2" style="display:none;"></div>
            </div>
        </div>
        <% } } %>

        <div class="chat-box">
            <div class="chat-scroll" id="chatScroll" style="min-height:400px;">
                <%
                    List<Map<String, String>> history = (List<Map<String, String>>) request.getAttribute("chatHistory");
                    if (history == null || history.isEmpty()) {
                %>
                <div class="text-center py-4" id="welcomeBlock">
                    <div class="stat-icon blue mx-auto mb-3" style="width:58px;height:58px;font-size:1.6rem;">
                        <i class="bi bi-stars"></i>
                    </div>
                    <h5 class="fw-bold">Xin chào! Tôi có thể giúp gì cho bạn?</h5>
                    <p class="text-muted mb-4" style="font-size:.92rem;">
                        Hỏi bất kỳ bài toán hay kiến thức nào — không giới hạn trong chương trình Vật lý 9.
                    </p>
                    <div class="text-start" style="max-width:620px;margin:0 auto;">
                        <div class="ai-suggestion" data-q="Giải giúp em bài: một vật nặng 5 kg rơi tự do từ độ cao 20 m. Tính vận tốc khi chạm đất.">
                            <i class="bi bi-calculator"></i> Giải giúp em bài: một vật nặng 5 kg rơi tự do từ độ cao 20 m.
                        </div>
                        <div class="ai-suggestion" data-q="Công thức định luật bảo toàn cơ năng là gì? Giải thích dễ hiểu cho học sinh lớp 9.">
                            <i class="bi bi-book"></i> Công thức định luật bảo toàn cơ năng là gì?
                        </div>
                        <div class="ai-suggestion" data-q="Một bóng đèn 220V-100W mắc nối tiếp với điện trở 100Ω. Tính cường độ dòng điện qua mạch.">
                            <i class="bi bi-lightbulb"></i> Tính dòng điện trong mạch đèn và điện trở mắc nối tiếp
                        </div>
                        <div class="ai-suggestion" data-q="Giải thích hiện tượng khúc xạ ánh sáng và cho ví dụ đời thường.">
                            <i class="bi bi-lightbulb"></i> Giải thích hiện tượng khúc xạ ánh sáng
                        </div>
                    </div>
                </div>
                <% } else {
                    for (Map<String, String> m : history) {
                        boolean isUser = "user".equals(m.get("role"));
                %>
                <div class="chat-msg <%= isUser ? "me" : "" %>">
                    <div class="chat-avatar <%= isUser ? "user" : "ai" %>">
                        <i class="bi <%= isUser ? "bi-person" : "bi-stars" %>"></i>
                    </div>
                    <div class="chat-bubble" data-md="<%= isUser ? "0" : "1" %>"><%= com.giasuly9.util.HtmlEscape.escape(m.get("content")) %></div>
                </div>
                <% } } %>
            </div>

            <div class="tutor-input-bar">
                <form id="chatForm" method="post" enctype="multipart/form-data"
                      action="${pageContext.request.contextPath}/ai-chat">
                    <!-- Ảnh đề bài đang chọn -->
                    <div id="imagePreviewWrap" class="mb-2 d-none">
                        <div class="d-inline-block position-relative">
                            <img id="imagePreview" class="img-preview" alt="Ảnh đề bài đã chọn">
                            <button type="button" class="btn btn-sm btn-danger rounded-circle position-absolute top-0 end-0 translate-middle"
                                    id="removeImage" title="Bỏ ảnh" style="width:26px;height:26px;padding:0;">
                                <i class="bi bi-x-lg" style="font-size:.7rem;"></i>
                            </button>
                        </div>
                        <div class="form-hint mt-1">
                            <i class="bi bi-image"></i> Ảnh sẽ được gửi kèm để AI đọc đề bài.
                        </div>
                    </div>

                    <div class="d-flex gap-2 align-items-end">
                        <div class="flex-grow-1">
                            <textarea class="form-control" name="message" id="messageInput" rows="2"
                                      placeholder="Nhập câu hỏi của bạn... (Ctrl + Enter để gửi)"></textarea>
                        </div>
                        <button type="button" class="btn btn-outline-primary px-3" id="pickImageBtn" title="Gửi ảnh đề bài">
                            <i class="bi bi-camera"></i>
                        </button>
                        <input type="file" name="questionImage" id="questionImage" accept="image/*" class="d-none">
                        <button type="submit" class="btn btn-primary px-4" id="sendBtn">
                            <i class="bi bi-send"></i> <span class="btn-label">Gửi</span>
                        </button>
                    </div>
                    <div class="form-hint mt-2">
                        <i class="bi bi-shield-check"></i>
                        Câu trả lời do AI tạo ra — hãy kiểm tra lại với giáo viên đối với bài tập trong thi cử.
                    </div>
                </form>
            </div>
        </div>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const CTX = '${pageContext.request.contextPath}';
    const scrollBox = document.getElementById('chatScroll');
    const form = document.getElementById('chatForm');
    const input = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');
    const btnLabel = sendBtn.querySelector('.btn-label');
    const welcomeBlock = document.getElementById('welcomeBlock');

    if (scrollBox) { scrollBox.scrollTop = scrollBox.scrollHeight; }

    // Render lại các tin nhắn AI đã lưu trong phiên (markdown + công thức)
    function renderStoredMessages() {
        if (!window.MdRender) { return; }
        document.querySelectorAll('.chat-bubble[data-md="1"]').forEach(el => {
            // Nội dung đã được escape ở server -> lấy textContent rồi render
            const raw = el.textContent;
            el.innerHTML = window.MdRender.render(raw);
            el.removeAttribute('data-md');
        });
    }
    renderStoredMessages();

    // Gợi ý nhanh
    document.querySelectorAll('.ai-suggestion').forEach(el => {
        el.addEventListener('click', () => {
            input.value = el.dataset.q;
            input.focus();
        });
    });

    // Ctrl + Enter để gửi
    input.addEventListener('keydown', e => {
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            e.preventDefault();
            sendMessage();
        }
    });

    function scrollToBottom() {
        if (scrollBox) { scrollBox.scrollTop = scrollBox.scrollHeight; }
    }

    function escapeHtml(s) {
        const d = document.createElement('div');
        d.textContent = s == null ? '' : s;
        return d.innerHTML;
    }

    function addMessage(role, content, file) {
        if (welcomeBlock) { welcomeBlock.remove(); }
        const wrap = document.createElement('div');
        wrap.className = 'chat-msg' + (role === 'user' ? ' me' : '');

        let imgHtml = '';
        if (file) {
            imgHtml = '<img class="chat-img mb-2" alt="Ảnh đã gửi">';
        }

        // Tin nhắn của AI có markdown + công thức LaTeX -> render đẹp.
        // Tin nhắn của học sinh giữ dạng text thuần cho an toàn.
        const bodyHtml = (role === 'assistant' && window.MdRender)
            ? window.MdRender.render(content)
            : escapeHtml(content);

        wrap.innerHTML =
            '<div class="chat-avatar ' + (role === 'user' ? 'user' : 'ai') + '">' +
            '<i class="bi ' + (role === 'user' ? 'bi-person' : 'bi-stars') + '"></i></div>' +
            '<div class="chat-bubble">' + imgHtml + bodyHtml + '</div>';

        if (file) {
            const img = wrap.querySelector('.chat-img');
            const reader = new FileReader();
            reader.onload = e => { img.src = e.target.result; };
            reader.readAsDataURL(file);
        }

        scrollBox.appendChild(wrap);
        scrollToBottom();
    }

    // ---- Chọn / xoá ảnh đề bài ----
    const pickBtn = document.getElementById('pickImageBtn');
    const imgInput = document.getElementById('questionImage');
    const imgPreviewWrap = document.getElementById('imagePreviewWrap');
    const imgPreview = document.getElementById('imagePreview');
    const removeImgBtn = document.getElementById('removeImage');

    function clearImage() {
        if (imgInput) { imgInput.value = ''; }
        if (imgPreviewWrap) { imgPreviewWrap.classList.add('d-none'); }
        if (imgPreview) { imgPreview.removeAttribute('src'); }
    }

    if (pickBtn && imgInput) {
        pickBtn.addEventListener('click', () => imgInput.click());
        imgInput.addEventListener('change', () => {
            const f = imgInput.files && imgInput.files[0];
            if (f) {
                const reader = new FileReader();
                reader.onload = e => {
                    imgPreview.src = e.target.result;
                    imgPreviewWrap.classList.remove('d-none');
                };
                reader.readAsDataURL(f);
            }
        });
    }
    if (removeImgBtn) {
        removeImgBtn.addEventListener('click', clearImage);
    }

    function setLoading(on) {
        sendBtn.disabled = on;
        input.disabled = on;
        btnLabel.textContent = on ? 'Đang suy nghĩ...' : 'Gửi';
        if (on) {
            let typing = document.getElementById('typingIndicator');
            if (!typing) {
                typing = document.createElement('div');
                typing.id = 'typingIndicator';
                typing.className = 'chat-msg';
                typing.innerHTML = '<div class="chat-avatar ai"><i class="bi bi-stars"></i></div>' +
                    '<div class="chat-bubble"><span class="spinner-border spinner-border-sm me-2"></span>AI đang trả lời...</div>';
                scrollBox.appendChild(typing);
                scrollToBottom();
            }
        } else {
            const typing = document.getElementById('typingIndicator');
            if (typing) { typing.remove(); }
        }
    }

    function sendMessage() {
        const message = input.value.trim();
        const fileInput = document.getElementById('questionImage');
        // PHẢI giữ tham chiếu file trước khi reset input, nếu không
        // fileInput.files sẽ bị xoá và FormData gửi đi giá trị undefined.
        const selectedFile = (fileInput && fileInput.files && fileInput.files.length > 0)
            ? fileInput.files[0] : null;

        if (!message && !selectedFile) { return; }

        addMessage('user', message, selectedFile);
        input.value = '';
        if (selectedFile) {
            clearImage();
        }
        setLoading(true);

        // Gửi multipart để kèm ảnh đề bài
        const fd = new FormData();
        fd.append('message', message);
        if (selectedFile) {
            fd.append('questionImage', selectedFile, selectedFile.name);
        }

        fetch(CTX + '/ai-chat', {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: fd
        })
        .then(r => r.json())
        .then(data => {
            setLoading(false);
            if (data && data.success) {
                addMessage('assistant', data.reply);
            } else {
                addMessage('assistant', (data && data.error) ? data.error : 'AI chưa phản hồi được. Vui lòng thử lại.');
            }
            input.focus();
        })
        .catch(err => {
            setLoading(false);
            addMessage('assistant', 'Không kết nối được máy chủ. Vui lòng thử lại.');
            console.error(err);
        });
    }

    form.addEventListener('submit', e => {
        e.preventDefault();
        sendMessage();
    });

    // ---- Lưu API key nhanh (admin) ----
    const keyForm = document.getElementById('keyForm');
    if (keyForm) {
        keyForm.addEventListener('submit', e => {
            e.preventDefault();
            const apiKey = document.getElementById('apiKeyInput').value.trim();
            const model = document.getElementById('modelInput').value.trim();
            const msgBox = document.getElementById('keyMsg');
            const btn = document.getElementById('keyBtn');
            if (!apiKey) {
                msgBox.style.display = 'block';
                msgBox.className = 'alert alert-warning py-2 mb-0';
                msgBox.textContent = 'Vui lòng nhập API key.';
                return;
            }
            btn.disabled = true;
            fetch(CTX + '/ai-chat/save-key', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: 'apiKey=' + encodeURIComponent(apiKey) + '&model=' + encodeURIComponent(model)
            })
            .then(r => r.json())
            .then(data => {
                btn.disabled = false;
                msgBox.style.display = 'block';
                if (data && data.success) {
                    msgBox.className = 'alert alert-success py-2 mb-0';
                    msgBox.textContent = data.message + ' Đang tải lại trang...';
                    setTimeout(() => location.reload(), 900);
                } else {
                    msgBox.className = 'alert alert-danger py-2 mb-0';
                    msgBox.textContent = (data && data.message) ? data.message : 'Không lưu được API key.';
                }
            })
            .catch(() => {
                btn.disabled = false;
                msgBox.style.display = 'block';
                msgBox.className = 'alert alert-danger py-2 mb-0';
                msgBox.textContent = 'Không kết nối được máy chủ.';
            });
        });
    }
</script>
</body>
</html>
