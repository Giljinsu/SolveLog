import { create } from "zustand";
import axios from "../context/axiosInstance.js";

export const useAlarmStore = create((set) => ({
  alarmList: [],
  alarmCount: 0,

  setAlarms: (alarms) => {
    set({
      alarmList: alarms,
      alarmCount: alarms.length > 0 ? alarms[0].alarmCnt : 0,
    });
  },

  clearAlarms: () => {
    set({
      alarmList: [],
      alarmCount: 0,
    });
  },

  getAlarm: async (username) => {
    const res = await axios.get(`/api/getAlarmList/${username}`);
    const alarms = res.data.data;

    set({
      alarmList: alarms,
      alarmCount: alarms.length > 0 ? alarms[0].alarmCnt : 0,
    });
  },
}));