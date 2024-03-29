require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "RNBlufiMesh"
  s.version      = package['version']
  s.summary      = "RNBlufiMesh"
  s.description  = <<-DESC
                  RNBlufiMesh
                   DESC
  s.homepage     = "https://github.com/elinter/etouch"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => package['repository']['url'] }
  s.source_files  = "ios/*.{h,m}"
  s.requires_arc = true
  s.vendored_libraries = "ios/libEtouch/*.{a}"
  $dir = File.dirname(__FILE__)
  $dir = $dir + "/ios/libEtouch/include/Etouch"
  s.pod_target_xcconfig = { "HEADER_SEARCH_PATHS" => $dir}
#  s.subspec "libEtouch" do |ss|
#    ss.source_files = "ios/libEtouch/**/*.{h,m}"
#  end
  s.dependency "React"

end

  
