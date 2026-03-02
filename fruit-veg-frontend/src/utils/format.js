/**
 * 格式化日期字符串
 * @param {string} dateStr ISO 格式日期 (如 2026-02-26T10:00:00)
 * @returns {string} 格式化后的日期 (如 2026-02-26)
 */
export const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};
