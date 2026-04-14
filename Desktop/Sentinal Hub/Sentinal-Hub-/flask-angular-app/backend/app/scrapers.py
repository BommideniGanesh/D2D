import requests
from bs4 import BeautifulSoup
from datetime import datetime, timedelta

def get_finviz_news(ticker):
    url = f"https://finviz.com/quote.ashx?t={ticker}"
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36'}

    try:
        response = requests.get(url, headers=headers, timeout=10)
        soup = BeautifulSoup(response.text, 'html.parser')
        news_table = soup.find(id='news-table')
        
        if not news_table:
            return []

        now = datetime.now()
        limit = now - timedelta(hours=24)
        rows = news_table.find_all('tr')
        news_data = []
        current_date = "Today"

        for row in rows:
            date_data = row.td.text.strip().split(' ')
            if len(date_data) == 2:
                current_date = date_data[0]
                time_str = date_data[1]
            else:
                time_str = date_data[0]

            try:
                date_str = now.strftime('%b-%d-%y') if current_date == 'Today' else current_date
                article_time = datetime.strptime(f"{date_str} {time_str}", "%b-%d-%y %I:%M%p")
                
                if article_time >= limit:
                    headline = row.a.get_text().strip()
                    link = row.a['href']
                    news_data.append({
                        "source": "Finviz",
                        "headline": headline,
                        "url": link,
                        "metadata": article_time.strftime("%I:%M%p")
                    })
                else: break
            except:
                continue
        return news_data
    except Exception as e:
        print(f"Error fetching Finviz: {e}")
        return []

def get_yahoo_news(ticker):
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36'}
    url = f"https://finance.yahoo.com/quote/{ticker}/"
    try:
        response = requests.get(url, headers=headers, timeout=10)
        soup = BeautifulSoup(response.text, 'html.parser')
        links = soup.find_all('a', class_='subtle-link')
        news_data = []

        for link_tag in links:
            try:
                title = link_tag.get('title') or link_tag.get_text().strip()
                href = link_tag.get('href')
                if not href or not title: continue
                if '/news/' not in href and '/video/' not in href and not href.startswith('/'): continue

                time_text = ""
                parent = link_tag.parent
                for _ in range(3):
                    if not parent: break
                    time_span = parent.find('span', string=lambda x: x and ('ago' in x.lower() or 'now' in x.lower()))
                    if time_span:
                        time_text = time_span.get_text().lower()
                        break
                    parent = parent.parent

                is_recent = not time_text or any(unit in time_text for unit in ["hour", "minute", "now"])
                if is_recent:
                    if href.startswith('/'): href = "https://finance.yahoo.com" + href
                    news_data.append({
                        "source": "Yahoo Finance",
                        "headline": title.replace(ticker, '').strip(),
                        "url": href,
                        "metadata": time_text if time_text else "Recently"
                    })
            except: continue

        unique_news = {}
        for v in news_data:
            if v['url'] not in unique_news and v['headline'].lower() not in ['view more', 'yahoo finance', ticker.lower()]:
                unique_news[v['url']] = v
        return list(unique_news.values())
    except Exception as e:
        print(f"Error fetching Yahoo: {e}")
        return []

def get_cnbc_news(ticker):
    url = f"https://www.cnbc.com/quotes/{ticker}"
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36'}
    try:
        response = requests.get(url, headers=headers, timeout=10)
        soup = BeautifulSoup(response.text, 'html.parser')
        headlines = soup.select('a.LatestNews-headline')
        
        news_items = []
        for item in headlines:
            title = item.get_text(strip=True)
            link = item['href']
            news_items.append({
                "source": "CNBC",
                "headline": title,
                "url": link if link.startswith('http') else f"https://www.cnbc.com{link}",
                "metadata": "Today"
            })
        return news_items
    except Exception as e:
        print(f"Error scraping CNBC: {e}")
        return []

def get_benzinga_news(ticker):
    url = f"https://www.benzinga.com/quote/{ticker}/news"
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'}
    try:
        response = requests.get(url, headers=headers, timeout=10)
        soup = BeautifulSoup(response.text, 'html.parser')
        articles = []
        links = soup.find_all('a', href=lambda x: x and '/news/' in x)
        
        for link in links:
            title = link.get_text().strip()
            if len(title) < 20: continue
                
            href = link['href']
            full_link = href if href.startswith('http') else f"https://www.benzinga.com{href}"
            
            container = link.find_parent('div')
            metadata = ""
            if container:
                text_parts = container.get_text("|").split("|")
                metadata = text_parts[-1].strip() if text_parts else "Recently"

            articles.append({
                "source": "Benzinga",
                "headline": title,
                "url": full_link,
                "metadata": metadata
            })
        return articles
    except Exception as e:
        print(f"Error scraping Benzinga: {e}")
        return []

def get_fool_news(ticker):
    url = f"https://www.fool.com/quote/nasdaq/{ticker.lower()}/"
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36'}
    try:
        response = requests.get(url, headers=headers, timeout=10)
        soup = BeautifulSoup(response.text, 'html.parser')
        container = soup.select_one('.p-6.pt-0.px-4.pb-6.md\\:px-10.md\\:pb-10')
        articles = []
        
        if container:
            all_links = container.find_all('a', href=True)
            for link in all_links:
                href = link.get('href', '')
                if "/author/" in href or "Previous" in link.text or "Next" in link.text: continue
                headline = link.get_text().strip()
                if not headline: continue

                full_url = href if href.startswith('http') else f"https://www.fool.com{href}"
                if not any(a['url'] == full_url for a in articles):
                    articles.append({
                        "source": "Motley Fool",
                        "headline": headline,
                        "url": full_url,
                        "metadata": "Recent"
                    })
        return articles
    except Exception as e:
        print(f"Error fetching Fool: {e}")
        return []
