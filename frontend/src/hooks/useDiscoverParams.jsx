import { useSearchParams } from "react-router-dom";
import { useMemo } from "react";

export function useDiscoverParams() {
  const [searchParams, setSearchParams] = useSearchParams();

  // --- Read ---
  const queryRaw = searchParams.get("query") ?? "";
  const genresRaw = searchParams.get("genres") ?? "";
  const statusRaw = searchParams.get("status") ?? "";
  const sortRaw = searchParams.get("sort") ?? "title";
  const pageRaw = searchParams.get("page") ?? "1";

  // uses memoize parsed arrays so identity is stable unless raw string changes
  const query = useMemo(() => queryRaw.trim(), [queryRaw]);
  const genres = useMemo(
    () => (genresRaw ? genresRaw.split(",").filter(Boolean) : []),
    [genresRaw]
  );
  const status = useMemo(
    () => (statusRaw ? statusRaw.split(",").filter(Boolean) : []),
    [statusRaw]
  );
  const sort = sortRaw;
  const page = useMemo(() => parseInt(pageRaw || "1", 10), [pageRaw]);

  // --- Write helpers ---
  // uses functional set to avoid problems with stale searchParams
  const setParam = (key, value) => {
    setSearchParams((prev) => {
      const params = new URLSearchParams(prev);
      const newVal = Array.isArray(value)
        ? value.join(",")
        : String(value ?? "");
      if (params.get(key) === newVal) return prev;
      if (!value || (Array.isArray(value) && value.length === 0)) {
        params.delete(key);
      } else {
        params.set(key, newVal);
      }
      return params;
    });
  };

  const updateParams = (updates) => {
    setSearchParams((prev) => {
      const params = new URLSearchParams(prev);
      let changed = false;

      Object.entries(updates).forEach(([key, value]) => {
        const current = params.get(key);

        if (
          value === undefined ||
          value === null ||
          value === "" ||
          (Array.isArray(value) && value.length === 0)
        ) {
          if (current !== null) {
            params.delete(key);
            changed = true;
          }
        } else {
          const newVal = Array.isArray(value) ? value.join(",") : String(value);
          if (current !== newVal) {
            params.set(key, newVal);
            changed = true;
          }
        }
      });

      // only trigger navigation if something actually changed
      return changed ? params : prev;
    });
  };

  return {
    query,
    genres,
    status,
    sort,
    page,
    setParam,
    updateParams,
  };
}
