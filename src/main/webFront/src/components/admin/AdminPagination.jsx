import './AdminPagination.css';

const DOTS = '...';

const range = (start, end) => {
  const result = [];
  for (let i = start; i <= end; i++) result.push(i);
  return result;
};

// currentPage1/totalPages/siblingCount 모두 1-based 기준으로 계산한다.
// 항상 첫 페이지·마지막 페이지를 포함하고, 현재 페이지 앞뒤로 siblingCount개를 보여주며
// 그 사이 생략 구간은 DOTS 하나로 표시한다. 반환 배열의 길이는 totalPages와 무관하게
// 최대 2 * siblingCount + 5 개로 고정된다.
const getPaginationItems = (currentPage1, totalPages, siblingCount) => {
  const firstPageIndex = 1;
  const lastPageIndex = totalPages;

  // 전체 페이지 수가 "양쪽 dots + 중간 siblingCount 창"이 필요로 하는 최대 개수 이하이면
  // dots를 쓸 이유가 없다 (경계 근처에서 페이지 번호가 중복 계산되는 것도 여기서 방지된다).
  const maxNumbersWithoutDots = 2 * siblingCount + 5;
  if (totalPages <= maxNumbersWithoutDots) {
    return range(firstPageIndex, lastPageIndex);
  }

  const leftSiblingIndex = Math.max(currentPage1 - siblingCount, firstPageIndex);
  const rightSiblingIndex = Math.min(currentPage1 + siblingCount, lastPageIndex);

  const shouldShowLeftDots = leftSiblingIndex > firstPageIndex + 1;
  const shouldShowRightDots = rightSiblingIndex < lastPageIndex - 1;

  if (!shouldShowLeftDots && !shouldShowRightDots) {
    return range(firstPageIndex, lastPageIndex);
  }

  if (!shouldShowLeftDots && shouldShowRightDots) {
    const leftItemCount = 2 * siblingCount + 1;
    return [...range(firstPageIndex, leftItemCount), DOTS, lastPageIndex];
  }

  if (shouldShowLeftDots && !shouldShowRightDots) {
    const rightItemCount = 2 * siblingCount + 1;
    return [firstPageIndex, DOTS, ...range(lastPageIndex - rightItemCount + 1, lastPageIndex)];
  }

  return [firstPageIndex, DOTS, ...range(leftSiblingIndex, rightSiblingIndex), DOTS, lastPageIndex];
};

// page: 0-based 현재 페이지 (백엔드 Pageable 규약과 동일)
// totalPages: 전체 페이지 수
// onPageChange: 0-based page를 인자로 받는 콜백 (예: setPage)
const AdminPagination = ({page, totalPages, onPageChange, siblingCount = 2}) => {
  if (!totalPages || totalPages <= 1) return null;

  const currentPage1 = page + 1;
  const items = getPaginationItems(currentPage1, totalPages, siblingCount);

  return (
      <div className="admin-pagination">
        <button
            type="button"
            className="admin-pagination__button admin-pagination__button--nav"
            disabled={page === 0}
            onClick={() => onPageChange(page - 1)}
            aria-label="이전 페이지"
        >
          {'<'}
        </button>

        {items.map((item, idx) => (
            item === DOTS ? (
                <span key={`dots-${idx}`} className="admin-pagination__ellipsis">{DOTS}</span>
            ) : (
                <button
                    type="button"
                    key={item}
                    className={
                      'admin-pagination__button' +
                      (item === currentPage1 ? ' admin-pagination__button--active' : '')
                    }
                    aria-current={item === currentPage1 ? 'page' : undefined}
                    onClick={() => onPageChange(item - 1)}
                >
                  {item}
                </button>
            )
        ))}

        <button
            type="button"
            className="admin-pagination__button admin-pagination__button--nav"
            disabled={page >= totalPages - 1}
            onClick={() => onPageChange(page + 1)}
            aria-label="다음 페이지"
        >
          {'>'}
        </button>
      </div>
  );
};

export default AdminPagination;
