import React, { useMemo, useState, useEffect } from "react";
import "./StatisticsPage.css";

const DAY_LABELS = ["일", "월", "화", "수", "목", "금", "토"];
const MONTH_LABELS = [
  "1월", "2월", "3월", "4월", "5월", "6월",
  "7월", "8월", "9월", "10월", "11월", "12월",
];

const getHeatLevelClass = (count) => {
  if (count === 0) return "heat-cell level-0";
  if (count <= 1) return "heat-cell level-1";
  if (count <= 2) return "heat-cell level-2";
  if (count <= 3) return "heat-cell level-3";
  return "heat-cell level-4";
};

const getMaxCount = (items) => Math.max(...items.map((item) => item.count), 1);

const formatDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const createYearlyHeatmapData = (year, apiMap) => {
  const startDate = new Date(year, 0, 1);
  const endDate = new Date(year, 11, 31);

  const dataMap = new Map();
  const cursor = new Date(startDate);

  while (cursor <= endDate) {
    const dateKey = formatDate(cursor);

    // 예시용 랜덤 데이터
//     const randomCount = [0, 0, 1, 1, 2, 3, 4, 5, 6, 7][
//       Math.floor(Math.random() * 10)
//     ];
    const count = apiMap.get(dateKey) || 0;

    dataMap.set(dateKey, {
      date: dateKey,
      count,
      month: cursor.getMonth(),
      dayOfWeek: cursor.getDay(),
      dateObj: new Date(cursor),
    });

    cursor.setDate(cursor.getDate() + 1);
  }

  const alignedCells = [];
  const firstDayOfWeek = startDate.getDay();

  for (let i = 0; i < firstDayOfWeek; i += 1) {
    alignedCells.push(null);
  }

  const current = new Date(startDate);
  while (current <= endDate) {
    alignedCells.push(dataMap.get(formatDate(current)));
    current.setDate(current.getDate() + 1);
  }

  while (alignedCells.length % 7 !== 0) {
    alignedCells.push(null);
  }

  const weeks = [];
  for (let i = 0; i < alignedCells.length; i += 7) {
    weeks.push(alignedCells.slice(i, i + 7));
  }

  const monthLabels = weeks.map((week, index) => {
    const monthStartCell = week.find(
      (cell) => cell !== null && cell.dateObj.getDate() === 1
    );

    if (monthStartCell) {
      return MONTH_LABELS[monthStartCell.month];
    }

    if (index === 0) {
      const firstDateCell = week.find((cell) => cell !== null);
      return firstDateCell ? MONTH_LABELS[firstDateCell.month] : "";
    }

    return "";
  });

  return { weeks, monthLabels };
};

const createSelectableYears = (range = 4) => {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: range }, (_, index) => currentYear - index);
};

function SummaryCard({ label, value }) {
  return (
    <div className="summary-card">
      <div className="summary-label">{label}</div>
      <div className="summary-value">{value}</div>
    </div>
  );
}

function StatBarList({ title, items }) {
  const [showAll, setShowAll] = useState(false);
  const max = getMaxCount(items);

  const DEFAULT_VISIBLE_COUNT = 5;
  const visibleItems = showAll ? items : items.slice(0, DEFAULT_VISIBLE_COUNT);
  const hasMore = items.length > DEFAULT_VISIBLE_COUNT;

  return (
    <div className="chart-card">
      <div className="section-title">{title}</div>

      <div className="bar-list">
        {visibleItems.map((item) => (
          <div className="bar-item" key={item.name}>
            <div className="bar-header">
              <span className="bar-name">{item.name}</span>
              <span className="bar-count">{item.count}</span>
            </div>

            <div className="bar-track">
              <div
                className="bar-fill"
                style={{ width: `${(item.count / max) * 100}%` }}
              />
            </div>
          </div>
        ))}
      </div>

      {hasMore && (
        <button
          type="button"
          className="stat-more-button"
          onClick={() => setShowAll((prev) => !prev)}
        >
          {showAll ? "접기" : `더보기 (${items.length - DEFAULT_VISIBLE_COUNT}개 더)`}
        </button>
      )}
    </div>
  );
}

function HeatmapSection({apiMap, getDailyStatistic, selectedYear, setSelectedYear}) {
  const selectableYears = useMemo(() => createSelectableYears(5), []);
//   const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [selectedCell, setSelectedCell] = useState(null);

  const { weeks, monthLabels } = useMemo(
    () => createYearlyHeatmapData(selectedYear, apiMap),
    [selectedYear, apiMap]
  );

  useEffect(() => {
    getDailyStatistic(selectedYear);
  }, [selectedYear]);

  return (
    <div className="heatmap-card">
      <div className="section-header heatmap-header-row">
        <div>
          <div className="section-title">연간 활동 히트맵</div>
          <div className="section-subtitle">
            {selectedYear}년 1월 1일부터 12월 31일까지의 문제풀이 활동입니다.
          </div>
        </div>

        <div className="year-select-box">
          <label htmlFor="heatmap-year" className="year-select-label">
            연도
          </label>
          <select
            id="heatmap-year"
            className="year-select"
            value={selectedYear}
            onChange={(e) => setSelectedYear(Number(e.target.value))}
          >
            {selectableYears.map((year) => (
              <option key={year} value={year}>
                {year}년
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="heatmap-wrapper">
        <div className="heatmap-layout">
          <div className="heatmap-day-labels">
            {DAY_LABELS.map((day, index) => (
              <div key={day} className="day-label">
                {index % 2 === 1 ? day : ""}
              </div>
            ))}
          </div>

          <div className="heatmap-main">
            <div
              className="heatmap-month-labels"
              style={{ gridTemplateColumns: `repeat(${weeks.length}, 16px)` }}
            >
              {monthLabels.map((label, index) => (
                <div key={`${label}-${index}`} className="month-label">
                  {label}
                </div>
              ))}
            </div>

            <div className="heatmap-weeks">
              {weeks.map((week, weekIndex) => (
                <div className="heatmap-week-column" key={weekIndex}>
                  {week.map((cell, dayIndex) => {
                    if (!cell) {
                      return (
                        <div
                          key={`${weekIndex}-${dayIndex}`}
                          className="heat-cell empty-cell"
                        />
                      );
                    }

                    return (
                      <div
                        key={cell.date}
                        className={getHeatLevelClass(cell.count)}
                        title={`${cell.date} : ${cell.count}개 풀이`}
                        onClick={() => setSelectedCell(cell)}
                      />
                    );
                  })}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="heatmap-legend">
        <span>적음</span>
        <div className="legend-box level-0" />
        <div className="legend-box level-1" />
        <div className="legend-box level-2" />
        <div className="legend-box level-3" />
        <div className="legend-box level-4" />
        <span>많음</span>
      </div>

      {selectedCell && (
        <div className="heatmap-detail">
          <div className="detail-title">
            {selectedCell.date}
          </div>

          <div className="detail-count">
            게시글 {selectedCell.count}개 작성
          </div>

          {selectedCell.count === 0 && (
            <div className="detail-empty">
              작성된 게시글이 없습니다.
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default function StatisticsPage({summaryData, tagStats, categoryStats, dailyStatistic,
  getDailyStatistic, setSelectedYear, selectedYear}) {

  const apiMap = useMemo(() => {
    const map = new Map();

    dailyStatistic.forEach((item) => {
      map.set(item.date, item.count);
    });

    return map;
  }, [dailyStatistic]);

  return (
    <div className="statistics-page">
      <div className="statistics-container">
        <div className="summary-grid">
          {summaryData.map((item) => (
            <SummaryCard
              key={item.label}
              label={item.label}
              value={item.value}
            />
          ))}
        </div>

        <HeatmapSection
          apiMap={apiMap}
          getDailyStatistic={getDailyStatistic}
          setSelectedYear={setSelectedYear}
          selectedYear={selectedYear}
        />

        <div className="bottom-grid">
          <StatBarList title="태그 통계" items={tagStats} />
          <StatBarList title="카테고리별 통계" items={categoryStats} />
        </div>
      </div>
    </div>
  );
}