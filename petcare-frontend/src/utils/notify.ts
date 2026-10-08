/**
 * 通知点击后的落地页。
 * 主人端的 /orders/:id 对员工/管理员会被路由守卫拦回首页，因此订单类通知必须按角色分流，
 * 异常类通知则分别落到两端各自的异常中心。返回 null 表示没有合适页面，只标记已读不跳转。
 */
export function notificationTarget(
  n: { refType?: string | null; refId?: number | null },
  isStaff: boolean,
  isAdmin: boolean,
): string | null {
  if (!n.refId) return null
  const userSide = !isStaff
  switch (n.refType) {
    case 'ORDER':
    case 'REPORT':
      if (userSide) return `/orders/${n.refId}`
      return isAdmin ? '/admin/orders' : '/staff/workbench'
    case 'EXCEPTION':
      if (userSide) return null
      return isAdmin ? '/admin/exceptions' : '/staff/exceptions'
    case 'REVIEW':
      // 后台暂无独立评价页，管理员落到订单管理；主人端无对应页面
      return isAdmin ? '/admin/orders' : null
    default:
      return null
  }
}
