// ✅ 공통 요청 유틸 (AccessToken 자동 재발급 포함)
console.log("✅ common.js 로드됨");

/**
 * AccessToken 자동 재발급 지원 fetch 유틸
 * @param {string} url 요청 URL
 * @param {object} options fetch 옵션
 */
async function fetchWithRefresh(url, options = {}) {
    console.log("🚀 [START] fetchWithRefresh 호출됨:", url);

    try {
        // 기본 옵션 설정
        options.credentials = "include"; // ✅ 쿠키 포함 (access_token / refresh_token)
        options.headers = options.headers || { "Content-Type": "application/json" };

        console.log("🟢 1. fetch 시작");
        const response = await fetch(url, options);
        console.log("🟢 2. fetch 완료 → 응답 상태:", response.status);

        // ✅ AccessToken 만료 시 (401 감지)
        if (response.status === 401) {
            console.warn("🟡 AccessToken 만료 감지 → RefreshToken으로 재발급 시도 중...");

            // refresh 요청
            const refreshRes = await fetch("/auth/refresh", {
                method: "POST",
                credentials: "include",
            });

            console.log("🟣 refresh 응답 상태:", refreshRes.status);

            // ✅ 새 AccessToken 재발급 성공 시 → 원래 요청 재시도
            if (refreshRes.ok) {
                console.log("🟢 새 AccessToken 재발급 성공 → 쿠키 반영 대기 중...");

                // ✅ 새 쿠키 브라우저 반영 기다리기 (약 0.3초)
                await new Promise((r) => setTimeout(r, 300));

                console.log("🔁 원래 요청 재시도 중...");

                // FormData / Stream 요청은 재시도 불가 처리
                const isStream =
                    options.body &&
                    (options.body instanceof FormData ||
                        options.body instanceof ReadableStream);

                if (isStream) {
                    console.warn("⚠️ Stream/FormData 요청은 자동 재시도 불가 → 페이지 리로드");
                    window.location.reload();
                    return refreshRes;
                }

                const retryRes = await fetch(url, {
                    ...options,
                    credentials: "include",
                });

                console.log("🔵 재시도 요청 완료 → 상태:", retryRes.status);

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

            // ❌ RefreshToken 만료 or 무효
            else {
                console.error("🔴 RefreshToken 만료 → 로그인 페이지 이동");
                alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                window.location.href = "/login";
                return refreshRes;
            }
        }

        // ✅ AccessToken 유효
        console.log("🟩 AccessToken 유효 → 정상 응답 반환");
        return response;

    } catch (err) {
        console.error("🔥 fetchWithRefresh 오류 발생:", err);
        throw err;
    } finally {
        console.log("🏁 fetchWithRefresh 종료");
    }
}
