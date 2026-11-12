/* =====================================================================
   Gia Sư Lý 9 - Render nội dung AI (Markdown + LaTeX)
   ---------------------------------------------------------------------
   Dùng chung cho: AI chat, kết quả bài tập, nhận xét của AI gia sư.
   - Markdown: tiêu đề, in đậm, in nghiêng, danh sách, bảng, code, link.
   - LaTeX: công thức $...$ (inline) và $$...$$ (block) render bằng KaTeX.
   - Luôn escape HTML trước khi render để tránh XSS.
   ===================================================================== */
(function (global) {
    'use strict';

    /* -----------------------------------------------------------------
       1. Escape HTML
       ----------------------------------------------------------------- */
    function escapeHtml(text) {
        if (text === null || text === undefined) { return ''; }
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /* -----------------------------------------------------------------
       2. Tách các đoạn công thức LaTeX ra trước, để không bị
          markdown engine "ăn mất" dấu _ hoặc * trong công thức.
       ----------------------------------------------------------------- */
    function extractMath(text) {
        const blocks = [];
        let out = text;

        // Công thức block: $$ ... $$
        out = out.replace(/\$\$([\s\S]+?)\$\$/g, function (m, tex) {
            blocks.push({ type: 'block', tex: tex });
            return '%%MATHBLOCK' + (blocks.length - 1) + '%%';
        });

        // Công thức inline: $ ... $ (không bắt được $ dùng một mình)
        out = out.replace(/\$([^$\n]+?)\$/g, function (m, tex) {
            blocks.push({ type: 'inline', tex: tex });
            return '%%MATHINLINE' + (blocks.length - 1) + '%%';
        });

        return { text: out, blocks: blocks };
    }

    function restoreMath(html, blocks) {
        let out = html;
        blocks.forEach(function (b, i) {
            if (b.type === 'block') {
                out = out.replace(
                    new RegExp('%%MATHBLOCK' + i + '%%', 'g'),
                    renderKatex(b.tex, true)
                );
            } else {
                out = out.replace(
                    new RegExp('%%MATHINLINE' + i + '%%', 'g'),
                    renderKatex(b.tex, false)
                );
            }
        });
        return out;
    }

    /* -----------------------------------------------------------------
       3. KaTeX - nếu chưa nạp thì trả về text gốc (an toàn)
       ----------------------------------------------------------------- */
    function renderKatex(tex, displayMode) {
        try {
            if (global.katex && typeof global.katex.renderToString === 'function') {
                return global.katex.renderToString(tex, {
                    displayMode: !!displayMode,
                    throwOnError: false,
                    strict: false,
                    trust: false
                });
            }
        } catch (e) {
            /* rơi xuống bên dưới */
        }
        // Chưa có KaTeX -> hiển thị dạng text, vẫn đọc được
        return displayMode
            ? '<div class="katex-fallback">' + escapeHtml('$$' + tex + '$$') + '</div>'
            : '<span class="katex-fallback">' + escapeHtml('$' + tex + '$') + '</span>';
    }

    /* -----------------------------------------------------------------
       4. Markdown tự viết (fallback khi không có marked.js)
          Đủ dùng cho: **đậm**, *nghiêng*, `code`, > trích, - danh sách,
          1. danh sách đánh số, # tiêu đề, --- gạch ngang, link.
       ----------------------------------------------------------------- */
    function simpleMarkdown(src) {
        const lines = src.split(/\r?\n/);
        const html = [];
        let inList = false;
        let inOrdered = false;

        function closeLists() {
            if (inList) { html.push('</ul>'); inList = false; }
            if (inOrdered) { html.push('</ol>'); inOrdered = false; }
        }

        for (let i = 0; i < lines.length; i++) {
            let line = lines[i];

            if (!line.trim()) { closeLists(); continue; }

            // Gạch ngang
            if (/^\s*([-*_])\s*\1\s*\1[\s\-*_]*$/.test(line)) {
                closeLists();
                html.push('<hr>');
                continue;
            }

            // Tiêu đề
            let m = line.match(/^(#{1,6})\s+(.*)$/);
            if (m) {
                closeLists();
                const lvl = Math.min(6, m[1].length + 2);
                html.push('<h' + lvl + '>' + inline(m[2]) + '</h' + lvl + '>');
                continue;
            }

            // Trích dẫn
            m = line.match(/^>\s?(.*)$/);
            if (m) {
                closeLists();
                html.push('<blockquote>' + inline(m[1]) + '</blockquote>');
                continue;
            }

            // Danh sách không đánh số
            m = line.match(/^\s*[-*+]\s+(.*)$/);
            if (m) {
                if (inOrdered) { html.push('</ol>'); inOrdered = false; }
                if (!inList) { html.push('<ul>'); inList = true; }
                html.push('<li>' + inline(m[1]) + '</li>');
                continue;
            }

            // Danh sách đánh số
            m = line.match(/^\s*\d+[.)]\s+(.*)$/);
            if (m) {
                if (inList) { html.push('</ul>'); inList = false; }
                if (!inOrdered) { html.push('<ol>'); inOrdered = true; }
                html.push('<li>' + inline(m[1]) + '</li>');
                continue;
            }

            // Đoạn văn: gom các dòng liên tiếp
            closeLists();
            const para = [line];
            while (i + 1 < lines.length && lines[i + 1].trim()
                    && !/^\s*([-*+]\s|\d+[.)]\s|#{1,6}\s|>|```)/.test(lines[i + 1])) {
                para.push(lines[++i]);
            }
            html.push('<p>' + inline(para.join('\n')).replace(/\n/g, '<br>') + '</p>');
        }

        closeLists();
        return html.join('\n');
    }

    /* -----------------------------------------------------------------
       5. Xử lý inline: code, bold, italic, link
       ----------------------------------------------------------------- */
    function inline(text) {
        let out = text;

        // Code inline (giữ nguyên, không áp dụng bold/italic bên trong)
        const codes = [];
        out = out.replace(/`([^`]+)`/g, function (m, c) {
            codes.push(c);
            return '%%CODE' + (codes.length - 1) + '%%';
        });

        // Link [text](url)
        out = out.replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g,
            '<a href="$2" target="_blank" rel="noopener">$1</a>');

        // Bold
        out = out.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        out = out.replace(/__([^_]+)__/g, '<strong>$1</strong>');

        // Italic
        out = out.replace(/(^|[^*])\*([^*\n]+)\*/g, '$1<em>$2</em>');
        out = out.replace(/(^|[^_\w])_([^_\n]+)_/g, '$1<em>$2</em>');

        // Khôi phục code
        out = out.replace(/%%CODE(\d+)%%/g, function (m, i) {
            return '<code>' + escapeHtml(codes[Number(i)]) + '</code>';
        });

        return out;
    }

    /* -----------------------------------------------------------------
       6. Hàm chính: render(text) -> HTML string
       ----------------------------------------------------------------- */
    function render(text) {
        if (text === null || text === undefined) { return ''; }
        let src = String(text);

        // Bỏ các thẻ markdown phản lồi của AI
        src = src.replace(/^```(?:json|text)?\s*/gm, '');

        const extracted = extractMath(src);
        let escaped = escapeHtml(extracted.text);

        let html;
        if (global.marked && typeof global.marked.parse === 'function') {
            try {
                global.marked.setOptions({ breaks: true, gfm: true });
                html = global.marked.parse(escaped);
            } catch (e) {
                html = simpleMarkdown(escaped);
            }
        } else {
            html = simpleMarkdown(escaped);
        }

        return restoreMath(html, extracted.blocks);
    }

    /* -----------------------------------------------------------------
       7. Render trực tiếp vào phần tử
       ----------------------------------------------------------------- */
    function renderInto(el, text) {
        if (!el) { return; }
        el.innerHTML = render(text);
        if (global.katex && typeof global.katex.render === 'function') {
            try {
                global.katex.render(el.textContent, el, {
                    displayMode: false, throwOnError: false
                });
            } catch (e) { /* bỏ qua */ }
        }
    }

    global.MdRender = {
        render: render,
        renderInto: renderInto,
        escapeHtml: escapeHtml
    };
})(window);
