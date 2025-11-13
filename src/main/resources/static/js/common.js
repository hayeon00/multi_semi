// ✅ 공통 요청 유틸 (AccessToken 자동 재발급 포함)
console.log("✅ common.js 로드됨");

let refreshPromise = null; // 🔒 중복 리프레시 방지용 전역 변수

/**
 * AccessToken 자동 재발급 지원 fetch 유틸 (Race Condition 방지 버전)
 */
async function fetchWithRefresh(url, options = {}) {
    console.log("🚀 [START] fetchWithRefresh 호출됨:", url);

    try {
        options.credentials = "include";
        options.headers = options.headers || { "Content-Type": "application/json" };

        console.log("🟢 1. fetch 시작");
        const response = await fetch(url, options);
        console.log("🟢 2. fetch 완료 → 응답 상태:", response.status);

        // ✅ AccessToken 만료 감지
        if (response.status === 401) {
            console.warn("🟡 AccessToken 만료 → RefreshToken으로 재발급 시도");

            // ⛔ 다른 요청이 이미 refresh 중이면 그 Promise를 기다린다
            if (!refreshPromise) {
                refreshPromise = (async () => {
                    const refreshRes = await fetch("/auth/refresh", {
                        method: "POST",
                        credentials: "include",
                    });
                    console.log("🟣 refresh 응답 상태:", refreshRes.status);
                    refreshPromise = null; // ✅ 완료되면 초기화
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

            console.log("🟢 새 AccessToken 재발급 성공 → 쿠키 반영 대기 중...");
            await new Promise((r) => setTimeout(r, 500)); // 쿠키 반영 대기

            console.log("🔁 원래 요청 재시도 중...");
            const retryRes = await fetch(url, { ...options, credentials: "include" });

            if (retryRes.ok) {
                console.log("✅ 재시도 성공 → 정상 응답 반환");
                return retryRes;
            } else {
                console.warn("⚠️ 재시도 실패 → 로그인 페이지로 이동");
                alert("권한이 만료되었습니다. 다시 로그인해주세요.");
                window.location.href = "/login";
                return retryRes;
            }
        }

        // ✅ 정상 응답
        console.log("🟩 AccessToken 유효 → 정상 응답 반환");
        return response;

    } catch (err) {
        console.error("🔥 fetchWithRefresh 오류 발생:", err);
        throw err;
    } finally {
        console.log("🏁 fetchWithRefresh 종료");
    }
}
