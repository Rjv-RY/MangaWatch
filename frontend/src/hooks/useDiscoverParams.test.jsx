import { describe, it, expect } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { useDiscoverParams } from "./useDiscoverParams";

// Wraps the hook in a MemoryRouter so useSearchParams has a router context
// to read/write from. `initialEntries` seeds the starting URL — this is the
// frontend equivalent of setting up test data before a Java test runs.
function wrapper(initialEntries) {
  return function Wrapper({ children }) {
    return (
      <MemoryRouter initialEntries={initialEntries}>{children}</MemoryRouter>
    );
  };
}

describe("useDiscoverParams", () => {
  it("defaults to empty query, empty arrays, sort=title, page=1 on a bare URL", () => {
    const { result } = renderHook(() => useDiscoverParams(), {
      wrapper: wrapper(["/discover"]),
    });

    expect(result.current.query).toBe("");
    expect(result.current.genres).toEqual([]);
    expect(result.current.status).toEqual([]);
    expect(result.current.sort).toBe("title");
    expect(result.current.page).toBe(1);
  });

  it("trims whitespace from the query param", () => {
    const { result } = renderHook(() => useDiscoverParams(), {
      wrapper: wrapper(["/discover?query=%20naruto%20"]), // "  naruto  " URL-encoded
    });

    expect(result.current.query).toBe("naruto");
  });

  it("splits comma-separated genres into an array, dropping empties", () => {
    const { result } = renderHook(() => useDiscoverParams(), {
      wrapper: wrapper(["/discover?genres=Action,Comedy,"]), // trailing comma on purpose
    });

    expect(result.current.genres).toEqual(["Action", "Comedy"]);
  });

  it("parses page as a number, defaulting to 1 if missing or invalid", () => {
    const { result: withPage } = renderHook(() => useDiscoverParams(), {
      wrapper: wrapper(["/discover?page=3"]),
    });
    expect(withPage.current.page).toBe(3);

    const { result: noPage } = renderHook(() => useDiscoverParams(), {
      wrapper: wrapper(["/discover"]),
    });
    expect(noPage.current.page).toBe(1);
  });

  it("setParam writes a single param and removes it when value is empty", () => {
    const { result } = renderHook(() => useDiscoverParams(), {
      wrapper: wrapper(["/discover"]),
    });

    act(() => {
      result.current.setParam("query", "bleach");
    });
    expect(result.current.query).toBe("bleach");

    act(() => {
      result.current.setParam("query", "");
    });
    expect(result.current.query).toBe("");
  });

  it("setParam joins array values with commas", () => {
    const { result } = renderHook(() => useDiscoverParams(), {
      wrapper: wrapper(["/discover"]),
    });

    act(() => {
      result.current.setParam("genres", ["Action", "Drama"]);
    });
    expect(result.current.genres).toEqual(["Action", "Drama"]);
  });

  it("updateParams applies multiple changes atomically", () => {
    const { result } = renderHook(() => useDiscoverParams(), {
      wrapper: wrapper(["/discover"]),
    });

    act(() => {
      result.current.updateParams({
        genres: ["Action"],
        status: ["Ongoing"],
        sort: "author",
        page: 2,
      });
    });

    expect(result.current.genres).toEqual(["Action"]);
    expect(result.current.status).toEqual(["Ongoing"]);
    expect(result.current.sort).toBe("author");
    expect(result.current.page).toBe(2);
  });

  it("updateParams clears a key when given an empty array or empty string", () => {
    const { result } = renderHook(() => useDiscoverParams(), {
      wrapper: wrapper(["/discover?genres=Action,Comedy&query=naruto"]),
    });

    act(() => {
      result.current.updateParams({ genres: [], query: "" });
    });

    expect(result.current.genres).toEqual([]);
    expect(result.current.query).toBe("");
  });
});
