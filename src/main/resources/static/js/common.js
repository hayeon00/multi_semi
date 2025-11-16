// ✅ 공통 요청 유틸 (AccessToken 자동 재발급 포함)
console.log("✅ common.js 로드됨");

let refreshPromise = null; // 🔒 중복 리프레시 방지용 전역 변수

/**
 * AccessToken 자동 재발급 지원 fetch 유틸
 */
async function fetchWithRefresh(url, options = {}) {
    console.log("🚀 [START] fetchWithRefresh 호출됨:", url);

    try {
        options.credentials = "include";
        options.headers = options.headers || {"Content-Type": "application/json"};

        console.log("🟢 1. fetch 시작");
        const response = await fetch(url, options);
        console.log("🟢 2. fetch 완료 → 응답 상태:", response.status);

        // 401일 때 AccessToken 재발급 시도
        if (response.status === 401) {
            console.warn("🟡 AccessToken 만료 → RefreshToken으로 재발급 시도");

            if (!refreshPromise) {
                refreshPromise = (async () => {
                    const refreshRes = await fetch("/auth/refresh", {
                        method: "POST",
                        credentials: "include",
                    });
                    console.log("🟣 refresh 응답 상태:", refreshRes.status);
                    refreshPromise = null;
                    return refreshRes;
                })();
            }

            const refreshRes = await refreshPromise;

            if (!refreshRes.ok) {
                console.error("🔴 RefreshToken 만료 → 로그인 페이지 이동");
                alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                window.location.href = "/login";
                return refreshRes;
            }

            console.log("🟢 새 AccessToken 재발급 성공 → 쿠키 반영 대기");
            await new Promise((r) => setTimeout(r, 500));

            console.log("🔁 원래 요청 재시도 중...");
            const retryRes = await fetch(url, {...options, credentials: "include"});

            if (retryRes.ok) {
                console.log("✅ 재시도 성공");
                return retryRes;
            } else {
                console.warn("⚠️ 재시도 실패 → 로그인 페이지로 이동");
                alert("권한이 만료되었습니다. 다시 로그인해주세요.");
                window.location.href = "/login";
                return retryRes;
            }
        }

        // 정상 응답
        console.log("🟩 AccessToken 유효 → 정상 응답 반환");
        return response;

    } catch (err) {
        console.error("🔥 fetchWithRefresh 오류 발생:", err);
        alert("네트워크 오류가 발생했습니다.");
        throw err;
    } finally {
        console.log("🏁 fetchWithRefresh 종료");
    }
}

/**
 * 안전한 페이지 이동 + 에러코드별 alert 처리
 */
async function safeRedirect(url) {
    const res = await fetchWithRefresh(url);

    if (res.ok) {
        window.location.href = url;
        return;
    }
    if (res.status === 401) {

    } else if (res.status === 403) {
        alert("접근 권한이 없습니다.");
        console.warn("Forbidden:", res);
    } else if (res.status === 404) {
        alert("요청한 페이지를 찾을 수 없습니다.");
    } else {
        alert("페이지를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
        console.warn("safeRedirect: non-token error", res);
    }
}

/*
function showToast(message) {
    const toast = document.createElement("div");
    toast.className = "toast-message";
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add("hide");
        setTimeout(() => toast.remove(), 300);
    }, 2000);
}
*/
