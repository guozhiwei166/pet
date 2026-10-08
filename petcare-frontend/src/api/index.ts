import http, { type UserInfo } from './http'

export interface CaptchaData { captchaId: string; image: string }

export interface PetTag { id: number; name: string }
export interface Pet {
  id: number
  userId: number
  name: string
  species: 'CAT' | 'DOG'
  breed: string
  gender: 'M' | 'F'
  birthDate: string
  age?: number
  weight: number
  sterilized: number
  furColor: string
  photo: string
  temperament: string
  notes: string
  tags: string[]
}
export interface PetPayload {
  id?: number
  name: string
  species: string
  breed?: string
  gender?: string
  birthDate?: string
  weight?: number
  sterilized?: number
  furColor?: string
  photo?: string
  temperament?: string
  notes?: string
  tagIds?: number[]
}
export interface VaccineItem {
  id: number
  vaccineName: string
  inoculationDate: string
  expireDate: string
  org: string
  remark: string
  status: string
}
export interface HealthItem {
  id: number
  recordDate: string
  weight: number
  temperature: number
  mentalState: string
  appetite: string
  water: string
  poop: string
  isAbnormal: number
  remark: string
}
export interface GrowthData {
  totalBoardings: number
  totalDays: number
  currentWeight: number
  weightCurve: { date: string; weight: number }[]
  boardings: unknown[]
}

export default {
  // 认证
  captcha: () => http.get<never, CaptchaData>('/auth/captcha'),
  verifyCaptcha: (p: { captchaId: string; captchaCode: string }) => http.post('/auth/captcha/verify', p),
  register: (p: object) => http.post<never, object>('/auth/register', p),
  login: (p: { username: string; password: string; captchaId?: string; captchaCode?: string }) => http.post<never, { token: string; user: UserInfo }>('/auth/login', p),
  logout: () => http.post('/auth/logout'),
  me: () => http.get<never, UserInfo>('/auth/me'),
  updateProfile: (p: { nickname: string; avatar?: string }) => http.put<never, UserInfo>('/auth/profile', p),
  changePassword: (p: { oldPassword: string; newPassword: string; securityAnswer?: string }) => http.put('/auth/password', p),
  saveSecurityQuestion: (p: { question: string; answer: string }) => http.post('/auth/security-question', p),
  resetPassword: (p: { phone: string; captchaId: string; captchaCode: string; newPassword: string; securityAnswer?: string }) => http.post('/auth/reset-password', p),
  // 上传：前端预检 5MB（与后端 petcare.upload.max-size 一致，避免超限请求发出）
  upload: (form: FormData) => {
    const file = form.get('file') as File | null
    if (file && file.size > 5 * 1024 * 1024) {
      return Promise.reject(new Error('图片不能超过 5MB，请压缩后重试'))
    }
    return http.post<never, { url: string; fileId: number }>('/files/upload', form)
  },
  // 宠物
  pets: (keyword = '') => http.get<never, Pet[]>('/pets', { params: { keyword } }),
  petDetail: (id: number | string) => http.get<never, Pet>(`/pets/${id}`),
  savePet: (p: PetPayload) => http.post<never, Pet>('/pets', p),
  updatePet: (id: number, p: PetPayload) => http.put<never, Pet>(`/pets/${id}`, p),
  deletePet: (id: number) => http.delete(`/pets/${id}`),
  tagDict: () => http.get<never, PetTag[]>('/pet-tags'),
  vaccines: (petId: number | string) => http.get<never, VaccineItem[]>(`/pets/${petId}/vaccines`),
  saveVaccine: (petId: number | string, p: object) => http.post(`/pets/${petId}/vaccines`, p),
  updateVaccine: (petId: number | string, vid: number, p: object) => http.put(`/pets/${petId}/vaccines/${vid}`, p),
  deleteVaccine: (petId: number | string, vid: number) => http.delete(`/pets/${petId}/vaccines/${vid}`),
  health: (petId: number | string, days = 30) => http.get<never, HealthItem[]>(`/pets/${petId}/health`, { params: { days } }),
  growth: (petId: number | string) => http.get<never, GrowthData>(`/pets/${petId}/growth`),
  // 预订与订单
  availability: (p: { startDate: string; endDate: string }) =>
    http.post<never, AvailabilityItem[]>('/boarding/availability', p),
  createOrder: (p: object) => http.post<never, OrderInfo>('/boarding/orders', p),
  orders: (status = 'ALL', page = 1, size = 10) => http.get<never, OrderPage>('/orders', { params: { status, page, size } }),
  orderDetail: (id: number | string) => http.get<never, any>(`/orders/${id}`),
  payOrder: (id: number | string) => http.post(`/orders/${id}/pay`),
  cancelOrder: (id: number | string, reason: string) => http.post(`/orders/${id}/cancel`, { reason }),
  refundOrder: (id: number | string) => http.post(`/orders/${id}/refund`),
  upgradeRoom: (id: number | string, roomTypeId: number) =>
    http.post<never, { diffAmount: number; roomTypeName: string; totalAmount: number }>(`/orders/${id}/upgrade-room`, { roomTypeId }),
  services: () => http.get<never, ServiceItem[]>('/services'),
  // 房间/房型（管理端）
  roomMap: () => http.get<never, any[]>('/rooms/map'),
  allRoomTypes: () => http.get<never, any[]>('/room-types', { params: { all: true } }),
  deleteRoom: (id: number) => http.delete(`/rooms/${id}`),
  batchDeleteRooms: (ids: number[]) =>
    http.post<never, { deleted: string[]; skipped: string[] }>('/rooms/batch-delete', { ids }),
  batchCreateRooms: (p: { roomTypeId: number; floor: number; startNo: string; count: number }) =>
    http.post<never, string[]>('/rooms/batch', p),
  // 通知
  notifications: (unreadOnly = false) => http.get<never, NotificationItem[]>('/notifications', { params: { unreadOnly } }),
  unreadCount: () => http.get<never, number>('/notifications/unread-count'),
  readNotification: (id: number) => http.post(`/notifications/${id}/read`),
  readAllNotifications: () => http.post('/notifications/read-all'),
  // 日报 / 异常 / 评价
  dailyReports: (orderId: number | string) => http.get<never, DailyReport[]>(`/orders/${orderId}/daily-reports`),
  reviewOrder: (id: number | string, p: object) => http.post(`/orders/${id}/review`, p),
  petReviews: (petId: number | string) => http.get<never, ReviewItem[]>(`/pets/${petId}/reviews`),
  reportException: (p: object) => http.post('/exceptions', p),
  exceptions: (status = 'ALL') => http.get<never, ExceptionItem[]>('/exceptions', { params: { status } }),
  transitionException: (id: number, action: string, note?: string) =>
    http.post(`/exceptions/${id}/${action}`, { note }),
  // 门店信息
  storeProfile: () => http.get<never, any>('/store/profile'),
  saveStoreProfile: (p: object) => http.put('/store/profile', p),
  // 服务预约
  createServiceBooking: (p: object) => http.post<never, any>('/service-bookings', p),
  myServiceBookings: () => http.get<never, any[]>('/service-bookings/mine'),
  allServiceBookings: (date: string) => http.get<never, any[]>('/service-bookings', { params: { date } }),
  transitionBooking: (id: number, action: string) => http.post(`/service-bookings/${id}/${action}`),
  // 优惠券
  availableCoupons: () => http.get<never, any[]>('/coupons/available'),
  myCoupons: () => http.get<never, any[]>('/coupons/mine'),
  claimCoupon: (id: number) => http.post(`/coupons/${id}/claim`),
  allCoupons: () => http.get<never, any[]>('/admin/coupons'),
  saveCoupon: (p: object) => http.post('/admin/coupons', p),
  deleteCoupon: (id: number) => http.delete(`/admin/coupons/${id}`),
  // 公告
  announcements: (all = false) => http.get<never, any[]>('/announcements', { params: { all } }),
  saveAnnouncement: (p: object) => http.post('/admin/announcements', p),
  deleteAnnouncement: (id: number) => http.delete(`/admin/announcements/${id}`),
  // 故事墙
  stories: () => http.get<never, any[]>('/stories'),
  publishStory: (p: object) => http.post('/stories', p),
  likeStory: (id: number) => http.post(`/stories/${id}/like`),
  allStories: () => http.get<never, any[]>('/admin/stories'),
  toggleStory: (id: number, status: number) => http.put(`/admin/stories/${id}/status`, { status }),
}

export interface AvailabilityItem {
  roomType: { id: number; name: string; species?: string; price: number; area: number; facilities: string; description: string; photo?: string; status?: number }
  totalRooms: number
  perDay: Record<string, number>
  minAvailable: number
  onSale: boolean
  bookable: boolean
}
export interface ServiceItem { id: number; name: string; price: number; unit: string; description: string }
export interface OrderInfo {
  id: number; orderNo: string; petId: number; roomTypeId: number; roomId?: number
  startDate: string; endDate: string; days: number
  roomAmount: number; serviceAmount: number; totalAmount: number
  status: string; remark?: string
}
export interface OrderPage { records: OrderInfo[]; total: number }
export interface NotificationItem {
  id: number; title: string; content: string; type: string; isRead: number
  refType?: string; refId?: number; createTime: string
}

export const ORDER_STATUS: Record<string, { text: string; type: string }> = {
  PENDING_PAYMENT: { text: '待支付', type: 'warning' },
  PAID: { text: '已支付', type: 'primary' },
  CONFIRMED: { text: '已预约', type: 'primary' },
  CHECKED_IN: { text: '已入住', type: 'success' },
  BOARDING: { text: '寄养中', type: 'success' },
  COMPLETED: { text: '已完成', type: 'info' },
  CANCELLED: { text: '已取消', type: 'info' },
  REFUNDING: { text: '退款中', type: 'warning' },
  REFUNDED: { text: '已退款', type: 'info' },
}
export interface DailyReport {
  id: number; orderId: number; petId: number; reportDate: string
  mentalState?: string; appetite?: string; water?: string; poop?: string
  isAbnormal: number; activityMinutes: number; mealsSummary?: string
  staffNote?: string; photos?: string
}
export interface ReviewItem {
  id: number; orderId: number; ratingOverall: number; content?: string
  tags?: string; reply?: string; createTime: string
}
export interface ExceptionItem {
  id: number; petId: number; petName?: string; orderId?: number; type: string
  level: string; description: string; status: string; createTime: string; handleNote?: string
}
