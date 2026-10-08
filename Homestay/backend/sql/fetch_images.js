// 图片批量下载器(仅下载,不改数据库)
// 读 _imglist.txt(格式: 表名|id|url),下载到 backend/uploads/images/,输出 _downloaded.txt
const fs = require('fs');
const path = require('path');

const LIST = process.argv[2] || 'C:/Users/L.L/Desktop/Homestay/_imglist.txt';
const OUT_DIR = path.resolve('C:/Users/L.L/Desktop/Homestay/backend/uploads/images');
const OUT_OK = 'C:/Users/L.L/Desktop/Homestay/_downloaded.txt';

async function main() {
  fs.mkdirSync(OUT_DIR, { recursive: true });
  const lines = fs.readFileSync(LIST, 'utf8').split('\n').filter(l => l.trim());
  let ok = 0, fail = 0;
  const okLines = [];
  for (const line of lines) {
    const [tbl, id, url] = line.split('|');
    if (!tbl || !url) continue;
    const fname = `${tbl}_${id}.jpg`;
    // 路径穿越防护:文件名必须是 "白名单表名_数字.jpg",且最终路径必须位于 OUT_DIR 内
    if (!/^[a-z_]+_\d+\.jpg$/.test(fname)) { fail++; continue; }
    const target = path.resolve(OUT_DIR, fname);
    if (!target.startsWith(OUT_DIR + path.sep)) { fail++; continue; }
    if (fs.existsSync(target) && fs.statSync(target).size > 1000) { ok++; okLines.push(`${tbl}|${id}|/uploads/images/${fname}`); continue; }
    try {
      const resp = await fetch(url, { redirect: 'follow', signal: AbortSignal.timeout(25000) });
      if (!resp.ok) throw new Error('HTTP ' + resp.status);
      const buf = Buffer.from(await resp.arrayBuffer());
      if (buf.length < 1000) throw new Error('too small');
      fs.writeFileSync(target, buf);
      ok++;
      okLines.push(`${tbl}|${id}|/uploads/images/${fname}`);
      if (ok % 25 === 0) console.log(`已下载 ${ok}...`);
    } catch (e) {
      fail++;
      console.log(`FAIL ${fname}: ${e.message}`);
    }
  }
  fs.writeFileSync(OUT_OK, okLines.join('\n'));
  console.log(`\n下载完成: 成功 ${ok}, 失败 ${fail}`);
}
main().catch(e => { console.error('FATAL', e.message); process.exit(1); });
