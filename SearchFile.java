/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.some.lucene;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class SearchFile {

  private SearchFile() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    String usage =
      "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/  details.";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }

   
    String indexVSM = "IndicesVSM";
    String indexBM25 = "IndicesBM25";
    String field = "contents";
    String queries = null;
    int repeat = 0;
    boolean raw = false;
    String queryString = null;
    int hitsPerPage = 10;
    
    for(int i = 0;i < args.length;i++) {
      if ("-index".equals(args[i])) {
        indexVSM = args[i+1];
        indexBM25 = args[i+1];
        i++;
      } else if ("-field".equals(args[i])) {
        field = args[i+1];
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = args[i+1];
        i++;
      } else if ("-query".equals(args[i])) {
        queryString = args[i+1];
        i++;
      } else if ("-repeat".equals(args[i])) {
        repeat = Integer.parseInt(args[i+1]);
        i++;
      } else if ("-raw".equals(args[i])) {
        raw = true;
      } else if ("-paging".equals(args[i])) {
        hitsPerPage = Integer.parseInt(args[i+1]);
        if (hitsPerPage <= 0) {
          System.err.println("There must be at least 1 hit per page.");
          System.exit(1);
        }
        i++;
      }
    }
    
    IndexReader readerVSM = DirectoryReader.open(FSDirectory.open(Paths.get(indexVSM)));
    IndexReader readerBM25 = DirectoryReader.open(FSDirectory.open(Paths.get(indexBM25)));
    IndexSearcher searcherVSM = new IndexSearcher(readerVSM);
    IndexSearcher searcherBM25 = new IndexSearcher(readerBM25);
    EnglishAnalyzer englishAnalyzer = new EnglishAnalyzer();
    BM25Similarity bm25Similarity = new BM25Similarity(1.2f, 0.5f);
    searcherBM25.setSimilarity(bm25Similarity);

    BufferedReader in = null;
    if (queries != null) {
      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
    } else {
      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }
    QueryParser parser = new QueryParser(field, englishAnalyzer);
      if (queries == null && queryString == null) {  
          File fileBM25 = new File("cranBM25.results");
          File fileVSM = new File("cranVSM.results");
          PrintWriter writerVSM = new PrintWriter(fileVSM, "UTF-8");
          PrintWriter writerBM25 = new PrintWriter(fileBM25, "UTF-8");
          
         
          BufferedReader br = new BufferedReader(new FileReader(new File("cran/cran.qry")));
  			String line;
  			String line4 = "";
  			int i = 0;
  			
  			while ((line = br.readLine()) != null) {
  				if(line.contains(".I")) {
  					if(line4 != "")
  					{
  					Query query1 = parser.parse(line4);
	                  
	                  TopDocs topDocsVSM = searcherVSM.search(query1, 1);
	                  TopDocs topDocsBM25 = searcherBM25.search(query1, 1);
	                  ScoreDoc[] hitsVSM = topDocsVSM.scoreDocs;
	                  ScoreDoc[] hitsBM25 = topDocsBM25.scoreDocs;
	                  for(ScoreDoc sd:hitsVSM)
	                  {
	                	   int s = sd.doc - 2;
							writerVSM.println((i) + " Q0 " + s + " 0 " + sd.score + " VSM ");   
	                	   System.out.println((i) + " Q0 " + s + " 0 " + sd.score + " VSM ");
//	               
	                  }
	                  for(ScoreDoc sd:hitsBM25)
	                  {
	                	   int s = sd.doc - 2;
	                	   writerBM25.println((i) + " Q0 " + s + " 0 " + sd.score + " BM25 ");   
	                	   System.out.println((i) + " Q0 " + s + " 0 " + sd.score + " BM25 ");
//	               
	                  }
	                  
  					}
  					i = i + 1;
  					line4 = "";
  					continue;
  				}else if(line.contains(".W")){
  					continue;
  				} else {
  					line4 += line.replace("*", "");
  					line4 = line4.replace("?", "");
  				}
  			}
//  			System.out.println(i + " - " + line4);
  			Query query1 = parser.parse(line4);
            
            TopDocs topDocsVSM = searcherVSM.search(query1, 10);
            TopDocs topDocsBM25 = searcherBM25.search(query1, 10);
            ScoreDoc[] hitsVSM = topDocsVSM.scoreDocs;
            ScoreDoc[] hitsBM25 = topDocsBM25.scoreDocs;
            for(ScoreDoc sd:hitsVSM)
            {
          	   int s = sd.doc;
          	   writerVSM.println((i) + " Q0 " + s + " 0 " + sd.score + " VSM");   
          	   System.out.println((i) + " Q0 " + s + " 0 " + sd.score + " VSM ");        
            }
            for(ScoreDoc sd:hitsBM25)
            {
          	   int s = sd.doc;
          	 writerBM25.println((i) + " Q0 " + s + " 0 " + sd.score + " BM25 ");   
          	   System.out.println((i) + " Q0 " + s + " 0 " + sd.score + " BM25 ");        
            }
            writerVSM.close();
                   
      }   
    readerVSM.close();
    readerBM25.close();
  }

  /**
   * This demonstrates a typical paging search scenario, where the search engine presents 
   * pages of size n to the user. The user can then go to the next page if interested in
   * the next hits.
   * 
   * When the query is executed for the first time, then only enough results are collected
   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
   * is executed another time and all hits are collected.
   * 
   */
  public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
                                     int hitsPerPage, boolean raw, boolean interactive) throws IOException {
 
    // Collect enough docs to show 5 pages
    TopDocs results = searcher.search(query, 5 * hitsPerPage);
    ScoreDoc[] hits = results.scoreDocs;
    
    int numTotalHits = Math.toIntExact(results.totalHits);
    System.out.println(numTotalHits + " total matching documents");

    int start = 0;
    int end = Math.min(numTotalHits, hitsPerPage);
        
    while (true) {
      if (end > hits.length) {
        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
        System.out.println("Collect more (y/n) ?");
        String line = in.readLine();
        if (line.length() == 0 || line.charAt(0) == 'n') {
          break;
        }

        hits = searcher.search(query, numTotalHits).scoreDocs;
      }
      
      end = Math.min(hits.length, start + hitsPerPage);
      
      for (int i = start; i < end; i++) {
        if (raw) {                              // output raw format
          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
          continue;
        }

        Document doc = searcher.doc(hits[i].doc);
        String path = doc.get("path");
        if (path != null) {
          System.out.println((i+1) + ". " + path);
          String title = doc.get("title");
          if (title != null) {
            System.out.println("   Title: " + doc.get("title"));
          }
        } else {
          System.out.println((i+1) + ". " + "No path for this document");
        }
                  
      }

      if (!interactive || end == 0) {
        break;
      }

      if (numTotalHits >= end) {
        boolean quit = false;
        while (true) {
          System.out.print("Press ");
          if (start - hitsPerPage >= 0) {
            System.out.print("(p)revious page, ");  
          }
          if (start + hitsPerPage < numTotalHits) {
            System.out.print("(n)ext page, ");
          }
          System.out.println("(q)uit or enter number to jump to a page.");
          
          String line = in.readLine();
          if (line.length() == 0 || line.charAt(0)=='q') {
            quit = true;
            break;
          }
          if (line.charAt(0) == 'p') {
            start = Math.max(0, start - hitsPerPage);
            break;
          } else if (line.charAt(0) == 'n') {
            if (start + hitsPerPage < numTotalHits) {
              start+=hitsPerPage;
            }
            break;
          } else {
            int page = Integer.parseInt(line);
            if ((page - 1) * hitsPerPage < numTotalHits) {
              start = (page - 1) * hitsPerPage;
              break;
            } else {
              System.out.println("No such page");
            }
          }
        }
        if (quit) break;
        end = Math.min(numTotalHits, start + hitsPerPage);
      }
    }
  }
}