<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.giasuly9.model.TeacherProfile" %>
<%@ page import="com.giasuly9.model.Lesson" %>
<%@ page import="com.giasuly9.model.LessonProgress" %>
<%@ page import="com.giasuly9.filter.SessionUtil" %>
<%
    List<TeacherProfile> teachers = (List<TeacherProfile>) request.getAttribute("teachers");
    TeacherProfile selected = (TeacherProfile) request.getAttribute("selectedTeacher");
    List<Lesson> lessons = (List<Lesson>) request.getAttribute("lessons");
    if (teachers == null) { teachers = java.util.Collections.emptyList(); }
    if (lessons == null) { lessons = java.util.Collections.emptyList(); }

    Map<Integer, LessonProgress> progressMap = (Map<Integer, LessonProgress>) request.getAttribute("progressMap");
    if (progressMap == null) { progressMap = java.util.Collections.emptyMap(); }

    int currentUserId = SessionUtil.currentUserId(request);
    boolean isLoggedIn = currentUserId > 0;
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Giáo viên Vật lý 9 | Gia Sư Lý</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb" style="font-size:.86rem;">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/home">Trang chủ</a></li>
                <li class="breadcrumb-item active">Giáo viên</li>
            </ol>
        </nav>

        <div class="page-head">
            <h2 class="page-title">Giáo viên Vật lý 9</h2>
            <div class="page-sub">Chọn thầy cô để xem các bài giảng video mà thầy/cô phụ trách</div>
        </div>

        <% if (teachers.isEmpty()) { %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-person-video3"></i>
                    <h5>Chưa có giáo viên</h5>
                    <p class="mb-0">Hồ sơ giáo viên sẽ được thêm sau.</p>
                </div>
            </div>
        <% } else { %>
            <div class="row g-3 mb-4">
                <%
                    for (TeacherProfile t : teachers) {
                        pageContext.setAttribute("t", t);
                        boolean isSel = (selected != null && selected.getUserId() == t.getUserId());
                %>
                <div class="col-md-6 col-lg-4">
                    <a class="teacher-card <%= isSel ? "active" : "" %>"
                       style="--tc: <%= t.getColor() != null ? t.getColor() : "#1d4ed8" %>;"
                       href="${pageContext.request.contextPath}/teachers?teacherId=${t.userId}">
                        <div class="teacher-head">
                            <% if (t.getAvatarUrl() != null && !t.getAvatarUrl().trim().isEmpty()) { %>
                                <img class="teacher-avatar" src="${t.avatarUrl}" alt="${t.fullName}">
                            <% } else { %>
                                <span class="teacher-avatar teacher-avatar-fallback">${t.initial}</span>
                            <% } %>
                            <div class="teacher-head-text">
                                <h5 class="teacher-name">${t.fullName}</h5>
                                <div class="teacher-degree">${t.degree}</div>
                                <div class="teacher-school"><i class="bi bi-geo-alt"></i> ${t.school}</div>
                            </div>
                        </div>

                        <div class="teacher-title">${t.title}</div>

                        <% if (t.getSpecialty() != null && !t.getSpecialty().trim().isEmpty()) { %>
                            <div class="teacher-tags">
                                <% for (String sp : t.getSpecialty().split(",")) { %>
                                    <% String s = sp.trim(); if (!s.isEmpty()) { %>
                                        <span class="badge-soft badge-teacher"><%= s %></span>
                                <% } } %>
                            </div>
                        <% } %>

                        <% if (t.getBio() != null && !t.getBio().trim().isEmpty()) { %>
                            <p class="teacher-bio">
                                <%= t.getBio().length() > 150 ? t.getBio().substring(0, 150) + "..." : t.getBio() %>
                            </p>
                        <% } %>

                        <div class="teacher-stats">
                            <span><i class="bi bi-collection-play"></i> <b>${t.videoCount}</b> video</span>
                            <span><i class="bi bi-award"></i> <b>${t.yearsExp}</b> năm kinh nghiệm</span>
                            <span><i class="bi bi-people"></i> <b>${t.studentCount}</b> học sinh</span>
                        </div>

                        <div class="teacher-cta">
                            <% if (isSel) { %>
                                <span class="btn btn-sm btn-light w-100">
                                    <i class="bi bi-check-circle"></i> Đang xem video của thầy/cô
                                </span>
                            <% } else { %>
                                <span class="btn btn-sm btn-cta w-100">
                                    <i class="bi bi-play-btn"></i> Xem ${t.videoCount} video
                                </span>
                            <% } %>
                        </div>
                    </a>
                </div>
                <% } %>
            </div>
        <% } %>

        <!-- ============ VIDEO CỦA GIÁO VIÊN ĐÃ CHỌN ============ -->
        <% if (selected != null) { %>
        <div class="card mb-4">
            <div class="card-head">
                <h5 class="card-title">
                    <i class="bi bi-play-btn"></i>
                    Video bài giảng của ${selected.fullName}
                </h5>
                <a href="${pageContext.request.contextPath}/teachers" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-x-lg"></i> Đóng
                </a>
            </div>
            <div class="card-body">
                <% if (lessons.isEmpty()) { %>
                    <div class="empty-state py-4">
                        <i class="bi bi-camera-video-off"></i>
                        <p class="mb-0">Thầy/cô chưa có bài giảng nào trong hệ thống.</p>
                    </div>
                <% } else { %>
                    <div class="row g-3">
                        <%
                            for (Lesson l : lessons) {
                                pageContext.setAttribute("l", l);
                                boolean isYoutube = "VIDEO_YOUTUBE".equals(l.getLessonType());
                                LessonProgress lp = progressMap.get(l.getLessonId());
                                int lpPercent = lp != null ? lp.getPercent() : 0;
                                boolean lpDone = lp != null && lp.isCompleted();
                        %>
                        <div class="col-md-6 col-lg-4">
                            <div class="lesson-card">
                                <div class="lesson-thumb">
                                    <span class="lesson-type">
                                        <% if (isYoutube) { %>
                                            <i class="bi bi-youtube"></i> YouTube
                                        <% } else if ("VIDEO_FILE".equals(l.getLessonType())) { %>
                                            <i class="bi bi-camera-video"></i> Video
                                        <% } else if ("WEBSITE".equals(l.getLessonType())) { %>
                                            <i class="bi bi-globe"></i> Website
                                        <% } else { %>
                                            <i class="bi bi-file-text"></i> Bài đọc
                                        <% } %>
                                    </span>
                                    <i class="bi <%= isYoutube ? "bi-play-circle" : ("TEXT".equals(l.getLessonType()) ? "bi-journal-text" : "bi-link-45deg") %>"></i>
                                    <% if (lpDone) { %>
                                        <span class="badge-soft badge-on" style="position:absolute;bottom:8px;right:9px;">
                                            <i class="bi bi-check-circle"></i> Đã xem
                                        </span>
                                    <% } %>
                                </div>
                                <div class="lesson-body">
                                    <h6 class="lesson-title">${l.title}</h6>
                                    <% if (l.getDescription() != null && !l.getDescription().trim().isEmpty()) { %>
                                        <p class="text-muted mb-2" style="font-size:.85rem;">
                                            <%= l.getDescription().length() > 90 ? l.getDescription().substring(0, 90) + "..." : l.getDescription() %>
                                        </p>
                                    <% } %>
                                    <div class="lesson-meta">
                                        <span><i class="bi bi-folder"></i> ${l.topicName}</span>
                                        <% if (l.getDurationMinutes() > 0) { %>
                                            <span><i class="bi bi-clock"></i> ${l.durationMinutes} phút</span>
                                        <% } %>
                                    </div>

                                    <% if (isYoutube || "VIDEO_FILE".equals(l.getLessonType())) { %>
                                        <div class="lesson-progress-bar <%= lpDone ? "done" : "" %>" id="bar-<%= l.getLessonId() %>">
                                            <span style="width:<%= lpPercent %>%"></span>
                                        </div>
                                        <div class="lesson-progress-text" id="txt-<%= l.getLessonId() %>">
                                            <span>Tiến độ xem</span>
                                            <b><%= lpPercent %>%<%= lpDone ? " - Hoàn thành" : "" %></b>
                                        </div>
                                    <% } %>

                                    <div class="d-flex gap-2 mt-3">
                                        <button class="btn btn-sm btn-primary flex-grow-1" data-bs-toggle="modal"
                                                data-bs-target="#tvModal<%= l.getLessonId() %>">
                                            <i class="bi bi-play-fill"></i> Xem
                                        </button>
                                        <a class="btn btn-sm btn-outline-primary"
                                           href="${pageContext.request.contextPath}/student/practice-setup?topicId=${l.topicId}&type=MCQ">
                                            Luyện
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <% } %>
                    </div>
                <% } %>
            </div>
        </div>

        <!-- Modal video -->
        <%
            for (Lesson l : lessons) {
                pageContext.setAttribute("l", l);
                boolean isYoutube = "VIDEO_YOUTUBE".equals(l.getLessonType());
                LessonProgress lp = progressMap.get(l.getLessonId());
                int lpPercent = lp != null ? lp.getPercent() : 0;
                boolean lpDone = lp != null && lp.isCompleted();
        %>
        <div class="modal fade" id="tvModal<%= l.getLessonId() %>" tabindex="-1">
            <div class="modal-dialog modal-lg modal-dialog-centered">
                <div class="modal-content" style="border-radius:16px;border:none;">
                    <div class="modal-header">
                        <h6 class="modal-title fw-bold">${l.title}</h6>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <% if (isYoutube && l.getVideoUrl() != null) { %>
                            <div class="video-frame mb-3">
                                <iframe src="${l.videoUrl}${l.videoUrl.contains('?') ? '&amp;' : '?'}enablejsapi=1&amp;rel=0"
                                        data-lesson="<%= l.getLessonId() %>"
                                        allowfullscreen
                                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"></iframe>
                            </div>
                            <div class="video-progress-hint">
                                <i class="bi bi-info-circle"></i>
                                <span>Tiến độ xem được ghi lại tự động.</span>
                                <span class="badge-soft badge-mcq" id="modalPct-<%= l.getLessonId() %>"><%= lpPercent %>%</span>
                            </div>
                            <div class="lesson-progress-bar <%= lpDone ? "done" : "" %>" id="modalBar-<%= l.getLessonId() %>">
                                <span style="width:<%= lpPercent %>%"></span>
                            </div>
                        <% } else if (l.getVideoUrl() != null && "VIDEO_FILE".equals(l.getLessonType())) { %>
                            <video id="vid-<%= l.getLessonId() %>" data-lesson="<%= l.getLessonId() %>" controls
                                   style="width:100%;border-radius:10px;" src="${l.videoUrl}"></video>
                            <div class="video-progress-hint">
                                <i class="bi bi-info-circle"></i>
                                <span>Tiến độ xem được ghi lại tự động.</span>
                                <span class="badge-soft badge-mcq" id="modalPct-<%= l.getLessonId() %>"><%= lpPercent %>%</span>
                            </div>
                            <div class="lesson-progress-bar <%= lpDone ? "done" : "" %>" id="modalBar-<%= l.getLessonId() %>">
                                <span style="width:<%= lpPercent %>%"></span>
                            </div>
                        <% } else if (l.getContent() != null && !l.getContent().trim().isEmpty()) { %>
                            <div class="p-3 rounded" style="background:#f8fafc;font-size:.93rem;line-height:1.75;white-space:pre-wrap;">${l.content}</div>
                        <% } %>

                        <% if (l.getDescription() != null && !l.getDescription().trim().isEmpty()) { %>
                            <p class="text-muted mt-3 mb-0" style="font-size:.88rem;">${l.description}</p>
                        <% } %>

                        <div class="d-flex gap-2 flex-wrap mt-3">
                            <% if (l.getVideoUrl() != null && !isYoutube) { %>
                                <a href="${l.videoUrl}" target="_blank" rel="noopener" class="btn btn-sm btn-outline-primary">
                                    <i class="bi bi-box-arrow-up-right"></i> Mở nguồn
                                </a>
                            <% } %>
                            <% if (l.getDocumentUrl() != null) { %>
                                <a href="${l.documentUrl}" target="_blank" rel="noopener" class="btn btn-sm btn-outline-primary">
                                    <i class="bi bi-box-arrow-up-right"></i> Mở YouTube
                                </a>
                            <% } %>
                            <a href="${pageContext.request.contextPath}/student/practice-setup?topicId=${l.topicId}&type=MCQ"
                               class="btn btn-sm btn-primary">
                                <i class="bi bi-pencil-square"></i> Luyện tập chủ đề này
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <% } %>
        <% } %>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function () {
        const ctx = '${pageContext.request.contextPath}';
        const isLoggedIn = <%= isLoggedIn %>;
        const progressUrl = ctx + '/student/lesson-progress';
        const saved = {};
        <%
            for (Lesson l : lessons) {
                pageContext.setAttribute("l", l);

                LessonProgress lp = progressMap.get(l.getLessonId());
                if (lp != null) {
        %>
        saved[<%= l.getLessonId() %>] = <%= lp.getPercent() %>;
        <% } } %>
        %>

        function applyProgress(lessonId, percent) {
            const bar = document.getElementById('bar-' + lessonId);
            const modalBar = document.getElementById('modalBar-' + lessonId);
            const txt = document.getElementById('txt-' + lessonId);
            const modalPct = document.getElementById('modalPct-' + lessonId);
            const done = percent >= 90;

            if (bar) {
                bar.querySelector('span').style.width = percent + '%';
                bar.classList.toggle('done', done);
            }
            if (modalBar) {
                modalBar.querySelector('span').style.width = percent + '%';
                modalBar.classList.toggle('done', done);
            }
            if (txt) {
                txt.innerHTML = '<span>Tiến độ xem</span><b>' + percent + '%' + (done ? ' - Hoàn thành' : '') + '</b>';
            }
            if (modalPct) {
                modalPct.textContent = percent + '%';
                modalPct.className = 'badge-soft ' + (done ? 'badge-on' : 'badge-mcq');
            }
        }

        function saveProgress(lessonId, percent) {
            if (!isLoggedIn) { return; }
            percent = Math.max(0, Math.min(100, Math.round(percent)));
            if (saved[lessonId] !== undefined && percent <= saved[lessonId] && percent < 90) {
                return;
            }
            saved[lessonId] = percent;

            const body = new URLSearchParams();
            body.append('lessonId', lessonId);
            body.append('percent', percent);

            fetch(progressUrl, {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                body: body.toString()
            }).then(r => r.json()).then(data => {
                if (data && data.ok) {
                    applyProgress(lessonId, data.percent);
                }
            }).catch(() => {});
        }

        document.querySelectorAll('video[data-lesson]').forEach(vid => {
            const lessonId = vid.dataset.lesson;
            vid.addEventListener('timeupdate', function () {
                if (!vid.duration || !isFinite(vid.duration)) { return; }
                const pct = (vid.currentTime / vid.duration) * 100;
                applyProgress(lessonId, pct);
                saveProgress(lessonId, pct);
            });
        });

        window.addEventListener('message', function (e) {
            if (!e.data || typeof e.data !== 'string') { return; }
            let data;
            try {
                data = JSON.parse(e.data);
            } catch (err) {
                return;
            }
            if (!data || !data.info || !data.info.videoId) { return; }

            const match = Array.from(document.querySelectorAll('.video-frame iframe')).map(f => {
                const src = f.getAttribute('src') || '';
                const m = src.match(/[?&]v=([^&]+)/) || src.match(/embed\/([^?]+)/);
                return (m && m[1] === data.info.videoId) ? f : null;
            }).filter(Boolean);

            if (match.length === 0) { return; }

            const lessonId = match[0].dataset.lesson;
            if (!lessonId || !data.info.duration) { return; }

            const pct = (data.info.currentTime / data.info.duration) * 100;
            applyProgress(lessonId, pct);
            saveProgress(lessonId, pct);
        }, false);

        document.querySelectorAll('.video-frame iframe').forEach(iframe => {
            iframe.addEventListener('load', function () {
                try {
                    iframe.contentWindow.postMessage('{"event":"listening","id":1,"channel":"widget"}', '*');
                } catch (err) {}
            });
        });
    })();
</script>
</body>
</html>
