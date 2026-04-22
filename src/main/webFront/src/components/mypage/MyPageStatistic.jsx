import StatisticsPage from "../common/StatisticsPage.jsx";
import {useEffect, useState, useRef} from "react";
import axios from "../../context/axiosInstance.js";


// const summaryData = [
//   { label: "전체 풀이", value: 152 },
//   { label: "올해 풀이", value: 87 },
//   { label: "이번 달 풀이", value: 12 },
// ];

// const tagStats = [
//   { name: "DP", count: 32 },
//   { name: "Graph", count: 21 },
//   { name: "Greedy", count: 15 },
//   { name: "BruteForce", count: 9 },
//   { name: "String", count: 5 },
// ];

const categoryStats = [
  { name: "문제풀이", count: 120 },
  { name: "자유게시판", count: 30 },
  { name: "학습기록", count: 15 },
];

const DAY_LABELS = ["일", "월", "화", "수", "목", "금", "토"];
const MONTH_LABELS = [
  "1월", "2월", "3월", "4월", "5월", "6월",
  "7월", "8월", "9월", "10월", "11월", "12월",
];

const MyPageStatistic = ({username, categoryType}) => {
  const [summaryData, setSummaryData] = useState([]);
  const [tagStats, setTagStats] = useState([]);
  const [categoryStats, setCategoryStats] = useState([]);
  const [dailyStatistic, setDailyStatistic] = useState([]);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const requestSeqRef = useRef(0);

  useEffect(() => {
    if (!categoryType) return;

    const timer = setTimeout(() => {
      const requestSeq = ++requestSeqRef.current;
      getUserStatistic(selectedYear, requestSeq);
    }, 150);

    return () => clearTimeout(timer);
  }, [categoryType]);

  const getDailyStatistic = async(year) => {
    try {
      const today = new Date();
      const localDate = new Date().toLocaleDateString('en-CA');

      const axiosResponse = await axios.get("/api/statistic/getDailySolveCountByYearAndUser", {
        params : {
          username: username,
          currentDate: localDate,
          ...(year && {
            year: year
          }),
          ...(categoryType !== "전체" && {
            categoryType: categoryType
          }),
        }
      });

      setDailyStatistic(axiosResponse.data.data);
    } catch (e) {
      console.log(e);
    }
  }

  const getUserStatistic = async (year, requestSeq) => {
    try {
      const today = new Date();
      const localDate = new Date().toLocaleDateString('en-CA');


      const axiosResponse = await axios.get("/api/statistic/getUserStatistic", {
        params : {
          username: username,
          currentDate: localDate,
          ...(year && {
            year: year
          }),
          ...(categoryType !== "전체" && {
            categoryType: categoryType
          }),
        }
      });

      // 가장 마지막 요청만 반영
      if (requestSeq !== requestSeqRef.current) return;

      setSummaryData([
        { label: "전체 작성 수", value: axiosResponse.data.totalCount },
        { label: "올해 작성 수", value: axiosResponse.data.yearCount },
        { label: "이번 달 작성 수", value: axiosResponse.data.monthCount },
      ])

      setTagStats(
        axiosResponse.data.tagStatistic.map(tag => ({
          name: tag.tagName,
          count: tag.count
        }))
      );

      setCategoryStats(
        axiosResponse.data.categoryStatistic.map(category => ({
          name: category.categoryType,
          count: category.count
        }))
      );

      setDailyStatistic(axiosResponse.data.dailyStatistic);
//       console.log(axiosResponse.data.dailyStatistic);
//       console.log(axiosResponse.data);
    } catch (e) {
      console.log(e);
    }
  }


  return (
    <div className={"my-page-statistic-section"}>
      <StatisticsPage
        summaryData={summaryData}
        tagStats={tagStats}
        categoryStats={categoryStats}
        dailyStatistic={dailyStatistic}
        getDailyStatistic={getDailyStatistic}
        selectedYear={selectedYear}
        setSelectedYear={setSelectedYear}
      />
    </div>
  )
}

export default MyPageStatistic;