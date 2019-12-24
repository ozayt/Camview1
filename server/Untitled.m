% while(true)
% FileID = fopen('out.txt');
% A = fscanf(FileID,'%c');
% A = uint8(A);
% B=A; 
% B = reshape(A,[300,300,3]);
% i=1
% while(i<(270000-2))
%    for x=1:300
%        for y=1:300
%            B(x,y,1) = A(i);
%            B(x,y,2) = A(i+1);
%            B(x,y,3) = A(i+2);
%            i = i+3 ;
%        end
%    end
%    
% end
% image(B)
% fclose(FileID);
% java.lang.Thread.sleep(1000);
% end
try
camview()
catch exception
camview()
end
