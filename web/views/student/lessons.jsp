<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.giasuly9.model.Lesson" %>
<%@ page import="com.giasuly9.model.LessonProgress" %>
<%@ page import="com.giasuly9.model.Topic" %>
<%
    Integer selectedTopicIdAttr = (Integer) request.getAttribute("selectedTopicId");
    int selectedTopicId = selectedTopicIdAttr != null ? selectedTopicIdAttr : 0;
    Map<Integer, LessonProgress> progressMap = (Map<Integer, LessonProgress>) request.getAttribute("progressMap");
    if (progressMap == null) { progressMap = java.util.Collections.emptyMap(); }
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bài giảng | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container">
        <div class="page-head">
            <h2 class="page-title">Bài giảng Vật lý 9</h2>
            <div class="page-sub">Chọn chủ đề để xem video, tài liệu — tiến độ xem được lưu lại tự động</div>
        </div>

        <div class="card mb-4">
            <div class="card-body">
                <label class="form-label">Lọc theo chủ đề</label>
                <div class="pill-nav">
                    <a class="pill <%= selectedTopicId == 0 ? "active" : "" %>"
                       href="${pageContext.request.contextPath}/student/lessons">Tất cả</a>
                    <%
                        List<Topic> topics = (List<Topic>) request.getAttribute("topics");
                        if (topics != null) {
                            for (Topic t : topics) {
                                pageContext.setAttribute("t", t);
                    %>
                    <a class="pill <%= selectedTopicId == t.getTopicId() ? "active" : "" %>"
                       href="${pageContext.request.contextPath}/student/lessons?topicId=${t.topicId}">
                        ${t.name}
                    </a>
                    <% } } %>
                </div>
            </div>
        </div>

        <%
            List<Lesson> lessons = (List<Lesson>) request.getAttribute("lessons");
            if (lessons == null || lessons.isEmpty()) {
        %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-collection-play"></i>
                    <h5>Chưa có bài giảng nào</h5>
                    <p class="mb-0">Giáo viên sẽ thêm bài giảng video và tài liệu cho chủ đề này.</p>
                </div>
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
                        <div class="lesson-thumb" data-lesson="<%= l.getLessonId() %>">
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
                                <% if (l.getCreatedByName() != null) { %>
                                    <span><i class="bi bi-person-video3"></i> ${l.createdByName}</span>
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
                                        data-bs-target="#lessonModal<%= l.getLessonId() %>">
                                    <i class="bi bi-play-fill"></i> Xem
                                </button>
                                <a class="btn btn-sm btn-outline-primary"
                                   href="${pageContext.request.contextPath}/student/practice-setup?topicId=${l.topicId}&type=ESSAY">
                                    Luyện
                                </a>
                            </div>

                            <%
                                boolean hasVideo = isYoutube || "VIDEO_FILE".equals(l.getLessonType());
                            %>
                            <div class="mt-2">
                                <button type="button"
                                        class="btn btn-sm w-100 js-mark-done <%= lpDone ? "btn-success" : "btn-outline-success" %>"
                                        data-lesson="<%= l.getLessonId() %>"
                                        data-done="<%= lpDone ? "1" : "0" %>">
                                    <i class="bi <%= lpDone ? "bi-check-circle-fill" : "bi-check2-circle" %>"></i>
                                    <span class="js-done-label"><%= lpDone ? "Đã học xong" : "Đánh dấu đã học" %></span>
                                </button>
                                <% if (!hasVideo) { %>
                                    <div class="text-muted mt-1" style="font-size:.74rem;">
                                        Bài này không có video — bấm nút trên để đánh dấu đã học.
                                    </div>
                                <% } %>
                            </div>
                        </div>
                    </div>
                </div>
                <% } %>
            </div>
        <% } %>
    </div>
</section>

<!-- Modal xem bài giảng -->
<%
    if (lessons != null) {
        for (Lesson l : lessons) {
            pageContext.setAttribute("l", l);
            boolean isYoutube = "VIDEO_YOUTUBE".equals(l.getLessonType());
            LessonProgress lp = progressMap.get(l.getLessonId());
            int lpPercent = lp != null ? lp.getPercent() : 0;
            boolean lpDone = lp != null && lp.isCompleted();
%>
<div class="modal fade" id="lessonModal<%= l.getLessonId() %>" tabindex="-1">
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
                        <span>Tiến độ được ghi lại khi bạn xem video. Xem hết sẽ được đánh dấu hoàn thành.</span>
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
                        <span>Tiến độ được ghi lại khi bạn xem video.</span>
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
                            <i class="bi bi-file-earmark-arrow-down"></i> Tài liệu
                        </a>
                    <% } %>
                    <a href="${pageContext.request.contextPath}/student/practice-setup?topicId=${l.topicId}&type=MCQ"
                       class="btn btn-sm btn-primary">
                        <i class="bi bi-pencil-square"></i> Luyện tập chủ đề này
                    </a>
                    <button type="button"
                            class="btn btn-sm js-mark-done-modal <%= lpDone ? "btn-success" : "btn-outline-success" %>"
                            data-lesson="<%= l.getLessonId() %>"
                            data-done="<%= lpDone ? "1" : "0" %>">
                        <i class="bi <%= lpDone ? "bi-check-circle-fill" : "bi-check2-circle" %>"></i>
                        <span class="js-done-label"><%= lpDone ? "Đã học xong" : "Đánh dấu đã học" %></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<% } } %>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function () {
        const ctx = '${pageContext.request.contextPath}';
        const progressUrl = ctx + '/student/lesson-progress';
        const saved = {};
        <%
            if (lessons != null) {
                for (Lesson l : lessons) {
                    pageContext.setAttribute("l", l);

                    LessonProgress lp = progressMap.get(l.getLessonId());
                    if (lp != null) {
        %>
        saved[<%= l.getLessonId() %>] = <%= lp.getPercent() %>;
        <% } } } %>

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

        // Video file: theo dõi thời gian phát
        document.querySelectorAll('video[data-lesson]').forEach(vid => {
            const lessonId = vid.dataset.lesson;
            vid.addEventListener('timeupdate', function () {
                if (!vid.duration || !isFinite(vid.duration)) { return; }
                const pct = (vid.currentTime / vid.duration) * 100;
                applyProgress(lessonId, pct);
                saveProgress(lessonId, pct);
            });
            // Xem hết video -> tự đánh dấu đã học
            vid.addEventListener('ended', function () {
                applyProgress(lessonId, 100);
                saveProgress(lessonId, 100);
                markDone(lessonId);
            });
        });

        // YouTube embed: dùng IFrame API chính thức để theo dõi thời gian phát
        // (postMessage thủ công không ổn định: thiếu duration, sai videoId, ...)
        let ytApiLoading = null;
        function loadYouTubeApi() {
            if (window.YT && window.YT.Player) { return Promise.resolve(); }
            if (ytApiLoading) { return ytApiLoading; }
            ytApiLoading = new Promise(function (resolve, reject) {
                const tag = document.createElement('script');
                tag.src = 'https://www.youtube.com/iframe_api';
                tag.onerror = function () { reject(new Error('Không tải được YouTube API')); };
                window.onYouTubeIframeAPIReady = function () { resolve(); };
                document.head.appendChild(tag);
            });
            return ytApiLoading;
        }

        const ytPlayers = {};

        function setupYouTubePlayer(iframe) {
            const lessonId = iframe.dataset.lesson;
            if (!lessonId || ytPlayers[lessonId]) { return; }

            loadYouTubeApi().then(function () {
                if (!window.YT || !window.YT.Player) { return; }
                let player;
                try {
                    player = new YT.Player(iframe, {
                        events: {
                            onStateChange: function (e) {
                                // e.data === 0 (YT.PlayerState.ENDED) -> xem xong
                                if (e.data === 0) {
                                    applyProgress(lessonId, 100);
                                    saveProgress(lessonId, 100);
                                    markDone(lessonId);
                                }
                            },
                            onReady: function (e) {
                                ytPlayers[lessonId] = e.target;
                            }
                        }
                    });
                } catch (err) {
                    /* iframe không hợp lệ -> bỏ qua */
                }
            }).catch(function () { /* không tải được API -> bỏ qua */ });

            // Gửi lệnh bật API cho iframe (dự phòng)
            iframe.addEventListener('load', function () {
                try {
                    iframe.contentWindow.postMessage('{"event":"listening","id":1,"channel":"widget"}', '*');
                } catch (err) {}
            });
        }

        document.querySelectorAll('.video-frame iframe').forEach(setupYouTubePlayer);

        // Theo dõi thời gian phát của player YouTube (poll nhẹ)
        setInterval(function () {
            for (const lessonId in ytPlayers) {
                const player = ytPlayers[lessonId];
                if (!player || typeof player.getCurrentTime !== 'function') { continue; }
                try {
                    const dur = player.getDuration();
                    const cur = player.getCurrentTime();
                    if (!dur || !isFinite(dur) || dur <= 0) { continue; }
                    const pct = Math.min(100, Math.round((cur / dur) * 100));
                    applyProgress(lessonId, pct);
                    saveProgress(lessonId, pct);
                } catch (err) { /* ignore */ }
            }
        }, 3000);

        // ---- Đánh dấu đã học (áp dụng cho mọi loại bài, kể cả bài không có video) ----
        function setDoneButtons(lessonId, done) {
            document.querySelectorAll('.js-mark-done[data-lesson="' + lessonId + '"], '
                    + '.js-mark-done-modal[data-lesson="' + lessonId + '"]').forEach(btn => {
                btn.dataset.done = done ? '1' : '0';
                btn.classList.toggle('btn-success', done);
                btn.classList.toggle('btn-outline-success', !done);
                const icon = btn.querySelector('i');
                if (icon) {
                    icon.className = 'bi ' + (done ? 'bi-check-circle-fill' : 'bi-check2-circle');
                }
                const label = btn.querySelector('.js-done-label');
                if (label) {
                    label.textContent = done ? 'Đã học xong' : 'Đánh dấu đã học';
                }
            });

            // Cập nhật thanh tiến độ và badge trên thẻ bài giảng
            const bar = document.getElementById('bar-' + lessonId);
            if (bar) {
                bar.querySelector('span').style.width = done ? '100%' : '0%';
                bar.classList.toggle('done', done);
            }
            const txt = document.getElementById('txt-' + lessonId);
            if (txt) {
                txt.innerHTML = '<span>Tiến độ xem</span><b>' + (done ? '100% - Hoàn thành' : '0%') + '</b>';
            }
            const modalBar = document.getElementById('modalBar-' + lessonId);
            if (modalBar) {
                modalBar.querySelector('span').style.width = done ? '100%' : '0%';
                modalBar.classList.toggle('done', done);
            }
            const modalPct = document.getElementById('modalPct-' + lessonId);
            if (modalPct) {
                modalPct.textContent = done ? '100%' : '0%';
                modalPct.className = 'badge-soft ' + (done ? 'badge-on' : 'badge-mcq');
            }

            // Badge "Đã xem" trên ảnh thumbnail
            const thumb = document.querySelector('.lesson-thumb[data-lesson="' + lessonId + '"]');
            if (thumb) {
                let badge = thumb.querySelector('.js-done-badge');
                if (done && !badge) {
                    badge = document.createElement('span');
                    badge.className = 'badge-soft badge-on js-done-badge';
                    badge.style.cssText = 'position:absolute;bottom:8px;right:9px;';
                    badge.innerHTML = '<i class="bi bi-check-circle"></i> Đã xem';
                    thumb.appendChild(badge);
                } else if (!done && badge) {
                    badge.remove();
                }
            }
        }

        /**
         * Đánh dấu bài đã học (từ server hoặc khi video xem xong).
         */
        function markDone(lessonId) {
            if (saved[lessonId] >= 100) { return; }
            setDoneButtons(lessonId, true);
            saved[lessonId] = 100;

            const body = new URLSearchParams();
            body.append('lessonId', lessonId);
            body.append('percent', 100);
            body.append('manual', '1');

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

        document.querySelectorAll('.js-mark-done, .js-mark-done-modal').forEach(btn => {
            btn.addEventListener('click', function () {
                const lessonId = this.dataset.lesson;
                const nowDone = this.dataset.done !== '1';

                if (nowDone) {
                    markDone(lessonId);
                    return;
                }

                // Bỏ đánh dấu -> ghi percent 0
                setDoneButtons(lessonId, false);
                saved[lessonId] = 0;

                const body = new URLSearchParams();
                body.append('lessonId', lessonId);
                body.append('percent', 0);
                body.append('manual', '1');

                fetch(progressUrl, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                    body: body.toString()
                }).then(r => r.json()).then(data => {
                    if (data && data.ok) {
                        applyProgress(lessonId, data.percent);
                    } else {
                        setDoneButtons(lessonId, true);
                    }
                }).catch(() => {
                    setDoneButtons(lessonId, true);
                });
            });
        });
    })();
</script>
</body>
</html>
